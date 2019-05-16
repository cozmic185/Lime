package lime.graphics.backgrounds

import lime.Lime
import lime.graphics.Texture
import lime.graphics.View
import kotlin.math.max

abstract class Background {
    val view = View()

    fun draw(texture: Texture, width: Float = texture.width, height: Float = texture.height, scrollX: Float = 0.0f, scrollY: Float = 0.0f) {
        val vx0 = max(width * scrollX, width - Lime.graphics.width)
        val vy0 = max(height * scrollY, height - Lime.graphics.height)
        val vx1 = vx0 + Lime.graphics.width
        val vy1 = vy0 + Lime.graphics.height

        val textureSize = Lime.graphics.textureSize
        val u0 = (vx0 + (texture.u0 * textureSize)) / textureSize
        val v0 = (vy0 + (texture.v0 * textureSize)) / textureSize
        val u1 = (vx1 + (texture.u0 * textureSize)) / textureSize
        val v1 = (vy1 + (texture.v0 * textureSize)) / textureSize

        view.draw(texture, 0.0f, 0.0f, Lime.graphics.width.toFloat(), Lime.graphics.height.toFloat(), u0 = u0, v0 = v0, u1 = u1, v1 = v1)
    }

    abstract fun frame(delta: Double)
}