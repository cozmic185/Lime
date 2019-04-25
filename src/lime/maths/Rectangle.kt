package lime.maths

import org.joml.Vector2f

data class Rectangle(var x0: Float, var y0: Float, var x1: Float, var y1: Float) {
    val width get() = x1 - x0
    val height get() = y1 - y0

    operator fun contains(other: Rectangle) = x0 <= other.x0 && y0 <= other.y0 && x1 >= other.x1 && y1 >= other.y1

    operator fun contains(point: Vector2f) = contains(point.x, point.y)

    fun contains(x: Float, y: Float) = x0 <= x && y0 <= y && x1 >= x && y1 >= y

    infix fun intersects(other: Rectangle) = x0 < other.x1 && x1 > other.x0 && y0 < other.y1 && y1 > other.y0
}