package lime.window

import lime.Lime
import lime.graphics.Image
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWImage
import org.lwjgl.system.MemoryStack.stackPush

class Cursor private constructor(internal val handle: Long) {
    companion object {
        private fun loadCursor(path: String, x: Int, y: Int): Long {
            val image = Image(path)
            val handle = stackPush().use {
                val glfwImage = GLFWImage.callocStack(it)
                glfwImage.width(image.width)
                glfwImage.height(image.height)
                glfwImage.pixels(image.pixels)
                glfwCreateCursor(glfwImage, x, y)
            }
            return handle
        }
    }

    enum class Shape {
        ARROW,
        IBEAM,
        CROSSHAIR,
        HAND,
        HRESIZE,
        VRESIZE
    }

    constructor(shape: Shape) : this(glfwCreateStandardCursor(when (shape) {
        Shape.ARROW -> GLFW_ARROW_CURSOR
        Shape.IBEAM -> GLFW_IBEAM_CURSOR
        Shape.CROSSHAIR -> GLFW_CROSSHAIR_CURSOR
        Shape.HAND -> GLFW_HAND_CURSOR
        Shape.HRESIZE -> GLFW_HRESIZE_CURSOR
        Shape.VRESIZE -> GLFW_VRESIZE_CURSOR
    }))

    constructor(path: String, x: Int, y: Int) : this(loadCursor(path, x, y))

    init {
        Lime.onShutdown {
            glfwDestroyCursor(handle)
        }
    }
}