package lime.graphics.fonts

import lime.graphics.ImageTexture
import lime.graphics.Texture

class GlyphTexture(imageTexture: ImageTexture, u0: Float, v0: Float, u1: Float, v1: Float, width: Float, height: Float) : Texture {
    override val u0 = ((u0 * width) + (imageTexture.u0 * imageTexture.textureSize)) / imageTexture.textureSize
    override val v0 = ((v0 * height) + (imageTexture.v0 * imageTexture.textureSize)) / imageTexture.textureSize
    override val u1 = ((u1 * width) + (imageTexture.u0 * imageTexture.textureSize)) / imageTexture.textureSize
    override val v1 = ((v1 * height) + (imageTexture.v0 * imageTexture.textureSize)) / imageTexture.textureSize
}