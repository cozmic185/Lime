package lime.graphics

import org.lwjgl.system.MemoryStack.stackPush
import lime.Lime
import lime.graphics.backgrounds.Background
import lime.graphics.fonts.Font
import lime.graphics.fonts.GlyphLayout
import lime.graphics.paths.Path
import lime.utils.Disposable
import lime.utils.Log
import org.lwjgl.opengl.GL30C.*
import org.lwjgl.stb.*
import org.lwjgl.stb.STBImageWrite.stbi_write_png
import org.lwjgl.stb.STBRectPack.stbrp_init_target
import org.lwjgl.stb.STBRectPack.stbrp_pack_rects
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ByteBuffer
import java.time.Instant
import java.util.*
import kotlin.math.ceil

class Graphics internal constructor(private val adjustToContentScale: Boolean) : Disposable {
    companion object {
        const val SIZEOF_INDEX = Int.SIZE_BYTES
        const val SIZEOF_VERTEX = (Int.SIZE_BYTES * 2 + Int.SIZE_BYTES * 2 + Byte.SIZE_BYTES * 4)
    }

    enum class Mode {
        POINT_FILTERING,
        BILINEAR_FILTERING
    }

    var clearColor = Color(Color.TRANSPARENT)
        set(value) {
            field = Color(value)
        }

    var mode = Mode.POINT_FILTERING
        set(value) {
            field = value
            setTextureFilter()
        }

    var background: Background? = null

    internal var blankU = 0.0f
        private set

    internal var blankV = 0.0f
        private set

    var width = 0
        private set

    var height = 0
        private set

    var textureSize = 0
        private set

    private var vbo = 0
    private var ibo = 0
    private var texture = 0
    private var shader = 0
    private var projViewUniform = 0
    private var textureUniform = 0

    private var screenShotRequested = false
    private var drawDebugScreen = false

    private var debugFont: Font? = null
    private val debugView by lazy { View() }
    private val debugLayout by lazy { GlyphLayout() }
    private val debugBackgroundPath by lazy { Path() }
    private val debugBackgroundColor by lazy { Color(0.25f, 0.25f, 0.25f, 0.25f) }
    private var verticesCounter = 0
    private var indicesCounter = 0
    private var drawCalls = 0

    init {
        if (adjustToContentScale) {
            width = ceil(Lime.window.width / Lime.window.contentScaleX).toInt()
            height = ceil(Lime.window.height / Lime.window.contentScaleY).toInt()
        } else {
            width = Lime.window.width
            height = Lime.window.height
        }

        vbo = glGenBuffers()
        ibo = glGenBuffers()
        texture = glGenTextures()

        setTextureFilter()

        fun createShader(source: String, type: Int): Int {
            val shader = glCreateShader(type)
            glShaderSource(shader, source)
            glCompileShader(shader)
            if (glGetShaderi(shader, GL_COMPILE_STATUS) != GL_TRUE)
                Log.fail(this::class, "Failed to compile shader:\n${glGetShaderInfoLog(shader)}")
            return shader
        }

        val vertexShader = createShader(
            """
            #version 110

            attribute vec2 aPosition;
            attribute vec2 aTexcoord;
            attribute vec4 aColor;

            varying vec2 vTexcoord;
            varying vec4 vColor;

            uniform mat4 uProjView;

            void main() {
                gl_Position = uProjView * vec4(aPosition, 0.0, 1.0);
                vTexcoord = aTexcoord;
                vColor = aColor.abgr;
            }
        """, GL_VERTEX_SHADER
        )

        val fragmentShader = createShader(
            """
            #version 110

            varying vec2 vTexcoord;
            varying vec4 vColor;

            uniform sampler2D sTexture;

            void main() {
                gl_FragColor = texture2D(sTexture, vTexcoord) * vColor;
            }
        """, GL_FRAGMENT_SHADER
        )

        shader = glCreateProgram()
        glAttachShader(shader, vertexShader)
        glAttachShader(shader, fragmentShader)
        glLinkProgram(shader)
        glDetachShader(shader, vertexShader)
        glDetachShader(shader, fragmentShader)
        glDeleteShader(vertexShader)
        glDeleteShader(fragmentShader)
        if (glGetProgrami(shader, GL_LINK_STATUS) != GL_TRUE)
            Log.fail(this::class, "Failed to link program:\n${glGetProgramInfoLog(shader)}")

        textureUniform = glGetUniformLocation(shader, "sTexture")
        projViewUniform = glGetUniformLocation(shader, "uProjView")
    }

    fun beginFrame(delta: Double) {
        verticesCounter = 0
        indicesCounter = 0
        drawCalls = 0

        glClearColor(clearColor.r, clearColor.g, clearColor.b, clearColor.a)
        glViewport(0, 0, Lime.window.width, Lime.window.height)
        glClear(GL_COLOR_BUFFER_BIT)

        glEnable(GL_BLEND)
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
        glDepthMask(false)
        glCullFace(GL_BACK)

        val positionAttribute = glGetAttribLocation(shader, "aPosition")
        val texcoordAttribute = glGetAttribLocation(shader, "aTexcoord")
        val colorAttribute = glGetAttribLocation(shader, "aColor")

        glBindBuffer(GL_ARRAY_BUFFER, vbo)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ibo)

        glEnableVertexAttribArray(positionAttribute)
        glEnableVertexAttribArray(texcoordAttribute)
        glEnableVertexAttribArray(colorAttribute)

        glVertexAttribPointer(positionAttribute, 2, GL_FLOAT, false, SIZEOF_VERTEX, 0)
        glVertexAttribPointer(texcoordAttribute, 2, GL_FLOAT, false, SIZEOF_VERTEX, 2L * Int.SIZE_BYTES)
        glVertexAttribPointer(colorAttribute, 4, GL_UNSIGNED_BYTE, true, SIZEOF_VERTEX, 4L * Int.SIZE_BYTES)

        glUseProgram(shader)
        glBindTexture(GL_TEXTURE_2D, texture)

        background?.let {
            it.frame(delta)
            render(it.view)
        }
    }

    fun endFrame() {
        if (drawDebugScreen)
            debugFont?.let {
                debugLayout.font = it
                debugLayout.setText(
                    """
            FPS: ${String.format("%d", (1.0 / Lime.deltaTime).toInt())} (${String.format("%.2f", Lime.deltaTime * 1000)} ms)
            OpenGL Vendor:   ${glGetString(GL_VENDOR)}
            OpenGL Version:  ${glGetString(GL_VERSION)}
            OpenGL Renderer: ${glGetString(GL_RENDERER)}
            Texture Size:    $textureSize x $textureSize
            Draw Calls:      $drawCalls
            Drawn Vertices:  $verticesCounter
            Drawn Indices:   $indicesCounter
            Audio Sources:   ${Lime.audio.numActive}
            Memory Usage:    ${Runtime.getRuntime().let { ((it.totalMemory() - it.freeMemory()) shr 10) shr 10 }} MB
        """.trimIndent()
                )

                debugBackgroundPath.reset()
                debugBackgroundPath.rect(0.0f, 0.0f, debugLayout.width, debugLayout.height)

                debugView.drawFillPath(debugBackgroundPath, debugBackgroundColor)
                debugView.draw(debugLayout, 0.0f, 0.0f)
                render(debugView)

                drawDebugScreen = false
            }

        glUseProgram(0)
        glBindTexture(GL_TEXTURE_2D, 0)

        glBindBuffer(GL_ARRAY_BUFFER, 0)
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0)

        if (screenShotRequested) {
            val data = memAlloc(Lime.window.width * Lime.window.height * 3)
            glReadPixels(0, 0, Lime.window.width, Lime.window.height, GL_RGB, GL_UNSIGNED_BYTE, data)
            val fileName = "screenshot${Date.from(Instant.now()).time}.png"
            if (stbi_write_png(fileName, Lime.window.width, Lime.window.height, 3, data, 0))
                Log.info(this::class, "Saved screenshot to $fileName")
            memFree(data)

            screenShotRequested = false
        }
    }

    fun requestScreenShot() {
        screenShotRequested = true
    }

    fun render(view: View) {
        stackPush().use {
            glUniformMatrix4fv(projViewUniform, false, view.camera.projView.get(it.callocFloat(16)))

            view.flush { indices, vertices ->
                if (indices.remaining() > 0 && vertices.remaining() > 0) {
                    indicesCounter += indices.remaining() / SIZEOF_INDEX
                    verticesCounter += vertices.remaining() / SIZEOF_VERTEX

                    glBufferData(GL_ARRAY_BUFFER, vertices, GL_STREAM_DRAW)
                    glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STREAM_DRAW)

                    glDrawElements(GL_TRIANGLES, indices.remaining() / SIZEOF_INDEX, GL_UNSIGNED_INT, 0)

                    drawCalls++
                }
            }
        }
    }

    fun loadImageTextures(textures: Array<ImageTexture>) {
        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)

        val packContext = STBRPContext.create()
        val rects = STBRPRect.create(textures.size + 1)
        val nodes = STBRPNode.create(maxSize)

        rects[0].w(2)
        rects[0].h(2)

        textures.forEachIndexed { index, texture ->
            with(rects[index + 1]) {
                id(index)
                w((texture.image.width + 2).toShort())
                h((texture.image.height + 2).toShort())
            }
        }

        textureSize = 512
        var packed = false
        var firstTry = true

        do {
            if (!firstTry) {
                textureSize *= 2
                if (textureSize >= maxSize)
                    Log.fail(this::class, "Unable to load all texture files, insufficient memory")
            } else
                firstTry = false

            stbrp_init_target(packContext, textureSize, textureSize, nodes)
            if (stbrp_pack_rects(packContext, rects) == 1)
                packed = true
        } while (!packed)

        glBindTexture(GL_TEXTURE_2D, texture)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, textureSize, textureSize, 0, GL_RGBA, GL_UNSIGNED_BYTE, null as ByteBuffer?)

        blankU = 0.5f / textureSize
        blankV = 0.5f / textureSize

        stackPush().use {
            glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, 2, 2, GL_RGBA, GL_UNSIGNED_BYTE, it.ints(0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt(), 0xFFFFFFFF.toInt()))
        }

        rects.forEachIndexed { index, rect ->
            if (index > 0) {
                val texture = textures[rect.id()]
                val image = texture.image

                val x = rect.x() + 1
                val y = rect.y() + 1

                glTexSubImage2D(GL_TEXTURE_2D, 0, x, y, image.width, image.height, GL_RGBA, GL_UNSIGNED_BYTE, image.pixels)

                texture.setLoaded(x, y, textureSize)
            }
        }

        glBindTexture(GL_TEXTURE_2D, 0)
    }

    fun drawDebugScreen(font: Font) {
        debugFont = font
        drawDebugScreen = true
    }

    private fun setTextureFilter() {
        glBindTexture(GL_TEXTURE_2D, texture)
        when (mode) {
            Mode.POINT_FILTERING -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            }
            Mode.BILINEAR_FILTERING -> {
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_LINEAR)
                glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_LINEAR)
            }
        }
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    override fun dispose() {
        if (glIsBuffer(vbo))
            glDeleteBuffers(vbo)

        if (glIsBuffer(ibo))
            glDeleteBuffers(ibo)

        if (glIsTexture(texture))
            glDeleteTextures(texture)

        if (glIsProgram(shader))
            glDeleteProgram(shader)
    }
}