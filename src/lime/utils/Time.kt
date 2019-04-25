package lime.utils

import org.lwjgl.glfw.GLFW.glfwGetTime

object Time {
    enum class Unit(val factor: Double) {
        SECONDS(1.0),
        MILLISECONDS(1000.0),
        MICROSECONDS(1000000.0),
        NANOSECONDS(1000000000.0)
    }

    val current get() = glfwGetTime()

    fun convert(value: Double, from: Unit, to: Unit) = value * from.factor / to.factor

    inline fun durationOf(unit: Unit = Unit.SECONDS, block: () -> kotlin.Unit): Double {
        val begin = current
        block()
        return convert(current - begin, Unit.SECONDS, unit)
    }
}