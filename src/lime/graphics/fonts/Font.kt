package lime.graphics.fonts

import lime.graphics.Image
import lime.graphics.ImageTexture
import lime.io.FileUtils
import lime.utils.Log
import org.joml.Vector2f
import org.lwjgl.opengl.GL11C.*
import org.lwjgl.stb.*
import org.lwjgl.stb.STBTruetype.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.*

class Font(path: String, val size: Float = 16.0f) {
    var alignToIntegerCoordinates = true

    var ascent = 0.0f
        private set

    var descent = 0.0f
        private set

    var lineGap = 0.0f
        private set

    val imageTexture: ImageTexture

    private val charData = STBTTPackedchar.create(95)
    private val imageWidth: Int
    private val imageHeight: Int
    private val glyphs = hashMapOf<Char, GlyphTexture>()

    init {
        val data = FileUtils.loadAsset(path)

        stackPush().use {
            val info = STBTTFontinfo.mallocStack(it)
            val pAscent = it.mallocInt(1)
            val pDescent = it.mallocInt(1)
            val pLineGap = it.mallocInt(1)

            stbtt_InitFont(info, data)
            stbtt_GetFontVMetrics(info, pAscent, pDescent, pLineGap)

            val scale = stbtt_ScaleForPixelHeight(info, size)
            ascent = pAscent[0] * scale
            descent = pDescent[0] * scale
            lineGap = pLineGap[0] * scale
        }

        val maxSize = glGetInteger(GL_MAX_TEXTURE_SIZE)
        val packContext = STBTTPackContext.malloc()

        var width = 128
        var height = 128
        var counter = 0
        var packed = false

        var pixelsAlpha = memAlloc(width * height)

        do {
            if (counter++ > 0) {
                if (counter % 2 == 0)
                    width *= 2
                else
                    height *= 2
                pixelsAlpha = memRealloc(pixelsAlpha, width * height)
                if (size >= maxSize)
                    Log.fail(this::class, "Unable to load all glyphs, insufficient memory")
            }

            stbtt_PackBegin(packContext, pixelsAlpha, width, height, 0, 1, 0)
            if (stbtt_PackFontRange(packContext, data, 0, this.size, 32, charData))
                packed = true
            stbtt_PackEnd(packContext)
        } while (!packed)

        packContext.free()

        val pixels = memAlloc(width * height * Int.SIZE_BYTES)

        repeat(height) { y ->
            repeat(width) { x ->
                pixels.put(0xFF.toByte())
                pixels.put(0xFF.toByte())
                pixels.put(0xFF.toByte())
                pixels.put(pixelsAlpha.get(x + y * width))
            }
        }

        pixels.position(0)

        imageWidth = width
        imageHeight = height
        imageTexture = ImageTexture(Image(width, height, pixels))

        memFree(pixels)
    }

    fun getGlyphAndPosition(character: Char, glyph: Glyph, position: Vector2f) {
        stackPush().use {
            val x = it.floats(position.x)
            val y = it.floats(position.y)
            val quad = STBTTAlignedQuad.callocStack(it)

            stbtt_GetPackedQuad(charData, imageWidth, imageHeight, character.toInt() - 32, x, y, quad, alignToIntegerCoordinates)

            glyph.texture = glyphs.computeIfAbsent(character) {
                GlyphTexture(imageTexture, quad.s0(), quad.t0(), quad.s1(), quad.t1(), imageWidth.toFloat(), imageHeight.toFloat())
            }

            glyph.x = quad.x0()
            glyph.y = quad.y0() + descent
            glyph.width = quad.x1() - quad.x0()
            glyph.height = quad.y1() - quad.y0()

            position.x = x[0]
            position.y = y[0]
        }
    }
}