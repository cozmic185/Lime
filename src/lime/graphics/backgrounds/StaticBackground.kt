package lime.graphics.backgrounds

import lime.Lime
import lime.graphics.ImageTexture

class StaticBackground(private val path: String) : Background() {
    private val texture: ImageTexture? get() = Lime.assets[path]

    override fun frame(delta: Double) {
        texture?.let {
            draw(it)
        }
    }
}