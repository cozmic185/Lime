package lime.audio.formats

import java.io.InputStream

interface AudioFormat {
    fun createStream(stream: InputStream): AudioStream
}