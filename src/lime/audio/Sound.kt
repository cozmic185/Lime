package lime.audio

import lime.Lime

class Sound(val path: String) {
    internal var buffer = -1

    init {
        Lime.audio.loadSound(this)
    }

    fun play(volume: Float = 1.0f, loop: Boolean = false) = Lime.audio.play(this, volume, loop)
}