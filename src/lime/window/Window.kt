package lime.window

import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import lime.Lime
import lime.graphics.Image
import lime.io.FileUtils
import lime.utils.Disposable
import lime.utils.Log
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.opengl.GL
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.*

class Window(title: String, val width: Int, val height: Int) : Disposable {
    private var handle = 0L

    var title = title
        set(value) {
            glfwSetWindowTitle(handle, value)
            field = value
        }

    val isCloseRequested
        get() = glfwWindowShouldClose(handle)

    var isFocused = true
        private set

    val contentScaleX: Float
        get() {
            return stackPush().use {
                val x = it.callocFloat(1)
                glfwGetWindowContentScale(handle, x, null)
                x.get(0)
            }
        }

    val contentScaleY: Float
        get() {
            return stackPush().use {
                val y = it.callocFloat(1)
                glfwGetWindowContentScale(handle, null, y)
                y.get(0)
            }
        }

    init {
        glfwSetErrorCallback { _, description ->
            Log.error(this::class, memUTF8(description))
        }

        if (!glfwInit())
            Log.fail(this::class, "Failed to initialize GLFW")

        val vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor())

        glfwDefaultWindowHints()
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE)
        vidMode?.let {
            glfwWindowHint(GLFW_RED_BITS, it.redBits())
            glfwWindowHint(GLFW_GREEN_BITS, it.greenBits())
            glfwWindowHint(GLFW_BLUE_BITS, it.blueBits())
            glfwWindowHint(GLFW_REFRESH_RATE, it.refreshRate())
        }
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 2)
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 0)

        handle = glfwCreateWindow(width, height, title, NULL, NULL)
        if (handle == 0L)
            Log.fail(this::class, "Failed to create Window")

        vidMode?.let {
            glfwSetWindowPos(handle, (it.width() - width) / 2, (it.height() - height) / 2)
        }

        glfwSetKeyCallback(handle) { _, key, _, action, _ ->
            when (action) {
                GLFW_PRESS -> Lime.input.onKeyDown(key)
                GLFW_RELEASE -> Lime.input.onKeyUp(key)
            }
        }

        glfwSetCursorPosCallback(handle) { _, x, y ->
            Lime.input.onMouseMoved(x.toInt(), y.toInt())
        }

        glfwSetScrollCallback(handle) { _, _, y ->
            Lime.input.onScrolled(y.toInt())
        }

        glfwSetMouseButtonCallback(handle) { _, button, action, _ ->
            when (action) {
                GLFW_PRESS -> Lime.input.onButtonDown(button)
                GLFW_RELEASE -> Lime.input.onButtonUp(button)
            }
        }

        glfwSetWindowFocusCallback(handle) { _, focused ->
            isFocused = focused
            Lime.events.dispatchEvent(FocusEvent(focused))
        }

        glfwMakeContextCurrent(handle)
        GL.createCapabilities()
        glfwSwapInterval(0)

        glfwShowWindow(handle)
    }

    fun pollEvents() = glfwPollEvents()

    fun swapBuffers() = glfwSwapBuffers(handle)

    fun setIcon(vararg paths: String) {
        stackPush().use {
            val glfwImages = GLFWImage.callocStack(paths.size, it)

            paths.forEachIndexed { index, path ->
                val image = try {
                    Image(path)
                } catch (e: Exception) {
                    Log.error(this::class, "Failed to load icon $path, using default icon")
                    Image(FileUtils.loadResource("lime.png"))
                }

                glfwImages[index].width(image.width)
                glfwImages[index].height(image.height)
                glfwImages[index].pixels(image.pixels)
            }

            glfwSetWindowIcon(handle, glfwImages)
        }
    }

    fun setCursor(cursor: Cursor) {
        glfwSetCursor(handle, cursor.handle)
    }

    override fun dispose() {
        glfwFreeCallbacks(handle)
        glfwDestroyWindow(handle)
        glfwTerminate()
    }
}