package lime.graphics

class ImageTexture(val image: Image) : Texture {
    override var u0 = 0.0f
        private set

    override var v0 = 0.0f
        private set

    override var u1 = 0.0f
        private set

    override var v1 = 0.0f
        private set

    var textureSize = 0
        private set

    internal fun setLoaded(x: Int, y: Int, size: Int) {
        textureSize = size
        val f = 1.0f / size
        u0 = x * f
        v0 = y * f
        u1 = (x + image.width) * f
        v1 = (y + image.height) * f
    }
}