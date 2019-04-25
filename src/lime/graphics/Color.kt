package lime.graphics

import lime.maths.clamp

data class Color(var r: Float = 1.0f, var g: Float = 1.0f, var b: Float = 1.0f, var a: Float = 1.0f) {
    companion object {
        val WHITE = Color(1.0f, 1.0f, 1.0f, 1.0f)
        val BLACK = Color(0.0f, 0.0f, 0.0f, 1.0f)
        val TRANSPARENT = Color(0.0f, 0.0f, 0.0f, 0.0f)
    }

    val bits: Int
        get() {
            val r = (clamp(r, 0.0f, 1.0f) * 0xFF).toInt()
            val g = (clamp(g, 0.0f, 1.0f) * 0xFF).toInt()
            val b = (clamp(b, 0.0f, 1.0f) * 0xFF).toInt()
            val a = (clamp(a, 0.0f, 1.0f) * 0xFF).toInt()
            return (r shl 24) or (g shl 16) or (b shl 8) or a
        }

    constructor(color: Color) : this(color.r, color.g, color.b, color.a)

    operator fun plus(color: Color): Color {
        val result = Color()
        result.r = r + color.r
        result.g = g + color.g
        result.b = b + color.b
        result.a = a + color.a
        return result
    }

    operator fun minus(color: Color): Color {
        val result = Color()
        result.r = r - color.r
        result.g = g - color.g
        result.b = b - color.b
        result.a = a - color.a
        return result
    }

    operator fun times(color: Color): Color {
        val result = Color()
        result.r = r * color.r
        result.g = g * color.g
        result.b = b * color.b
        result.a = a * color.a
        return result
    }

    operator fun div(color: Color): Color {
        val result = Color()
        result.r = r / color.r
        result.g = g / color.g
        result.b = b / color.b
        result.a = a / color.a
        return result
    }

    operator fun plusAssign(color: Color) {
        r += color.r
        g += color.g
        b += color.b
        a += color.a
    }

    operator fun minusAssign(color: Color) {
        r -= color.r
        g -= color.g
        b -= color.b
        a -= color.a
    }

    operator fun timesAssign(color: Color) {
        r *= color.r
        g *= color.g
        b *= color.b
        a *= color.a
    }

    operator fun divAssign(color: Color) {
        r /= color.r
        g /= color.g
        b /= color.b
        a /= color.a
    }
}