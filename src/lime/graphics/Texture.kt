package lime.graphics

import lime.Lime

interface Texture {
    val u0: Float
    val v0: Float
    val u1: Float
    val v1: Float

    val width get() = (u1 - u0) * Lime.graphics.textureSize
    val height get() = (v1 - v0) * Lime.graphics.textureSize
}