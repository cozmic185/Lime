package lime.audio

import org.lwjgl.openal.AL10.*

class AudioPlayer internal constructor(private val source: Int, volume: Float) {
    var volume = volume
        set(value) {
            if (source != -1)
                alSourcef(source, AL_GAIN, value)
            field = value
        }

    fun stop() {
        if (source != -1)
            alSourceStop(source)
    }

    fun stopLooping() {
        if (source != -1)
            alSourcei(source, AL_LOOPING, AL_FALSE)
    }
}