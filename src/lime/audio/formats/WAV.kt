package lime.audio.formats

import java.io.InputStream
import javax.sound.sampled.AudioSystem

object WAV : AudioFormat {
    class WAVAudioStream(stream: InputStream) : AudioStream {
        private val audioInputStream = AudioSystem.getAudioInputStream(stream)

        override val sampleSize = audioInputStream.format.sampleSizeInBits
        override val channels = audioInputStream.format.channels
        override val sampleRate = audioInputStream.format.sampleRate.toInt()
        override val isBigEndian = audioInputStream.format.isBigEndian
        override val remaining get() = audioInputStream.available()

        override fun read(buffer: ByteArray): Int {
            audioInputStream.frameLength
            return audioInputStream.read(buffer)
        }

        override fun close() {
            audioInputStream.close()
        }
    }

    override fun createStream(stream: InputStream) = WAVAudioStream(stream)
}