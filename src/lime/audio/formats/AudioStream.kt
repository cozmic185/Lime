package lime.audio.formats

interface AudioStream : AutoCloseable {
    val sampleSize: Int
    val channels: Int
    val sampleRate: Int
    val isBigEndian: Boolean
    val remaining: Int

    fun read(buffer: ByteArray): Int
}