package lime.audio

import lime.Lime
import lime.audio.formats.AudioFormat
import lime.audio.formats.WAV
import lime.io.FileUtils
import lime.utils.*
import org.lwjgl.openal.AL
import org.lwjgl.openal.AL10.*
import org.lwjgl.openal.ALC
import org.lwjgl.openal.ALC10.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.memAlloc
import org.lwjgl.system.MemoryUtil.memFree
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.IntBuffer

class Audio internal constructor(updateRate: Int) : Disposable {
    private class Source : Disposable {
        val handle: Int
        val state get() = alGetSourcei(handle, AL_SOURCE_STATE)

        init {
            val handle = alGenSources()
            this.handle = if (alGetError() == AL_NO_ERROR)
                handle
            else
                -1
        }

        override fun dispose() {
            if (handle != -1)
                alDeleteSources(handle)
        }
    }

    private class Buffer : Disposable {
        val handle = alGenBuffers()

        fun setData(data: ByteArray, count: Int, isBigEndian: Boolean, format: Int, sampleRate: Int) {
            val buffer = memAlloc(count)
            val srcBuffer = ByteBuffer.wrap(data, 0, count)
            srcBuffer.order(if (isBigEndian) ByteOrder.BIG_ENDIAN else ByteOrder.LITTLE_ENDIAN)

            if (format == AL_FORMAT_MONO16 || format == AL_FORMAT_STEREO16) {
                val bufferShort = buffer.asShortBuffer()
                val srcBufferShort = srcBuffer.asShortBuffer()

                while (srcBufferShort.hasRemaining())
                    bufferShort.put(srcBufferShort.get())
            } else {
                while (srcBuffer.hasRemaining())
                    buffer.put(srcBuffer.get())
            }

            buffer.position(0)
            alBufferData(handle, format, buffer, sampleRate)
            memFree(buffer)
        }

        override fun dispose() {
            alDeleteBuffers(handle)
        }
    }

    var updateRate = updateRate
        set(value) {
            field = value
            updater.rate = value
        }

    internal var noDevice = false

    private var device = 0L
    private var context = 0L
    private var updater = Updater(updateRate, ::update)
    private val sources = Pool { Source() }
    private val activeSources = Bag<Source>()
    private val formats = hashMapOf<String, AudioFormat>()
    private val queue = Bag<() -> Unit>()
    private val buffers = Bag<Buffer>()

    init {
        device = alcOpenDevice(null as ByteBuffer?)

        if (device == 0L)
            Log.error(this::class, "Failed to open OpenAL device")
        else {
            val deviceCapabilities = ALC.createCapabilities(device)
            context = alcCreateContext(device, null as IntBuffer?)

            if (context == 0L) {
                alcCloseDevice(device)
                Log.error(this::class, "Failed to create OpenAL context")
                noDevice = true
            }

            if (!alcMakeContextCurrent(context)) {
                alcDestroyContext(context)
                alcCloseDevice(device)
                Log.error(this::class, "Failed to make OpenAL context current")
                noDevice = true
            }

            if (!noDevice) {
                AL.createCapabilities(deviceCapabilities)

                stackPush().use {
                    alListenerfv(AL_ORIENTATION, it.floats(0.0f, 0.0f, 0.0f, 0.0f, -1.0f, 0.0f))
                    alListenerfv(AL_VELOCITY, it.floats(0.0f, 0.0f, 0.0f))
                    alListenerfv(AL_POSITION, it.floats(0.0f, 0.0f, 0.0f))
                }

                registerFormat("wav", WAV)

                Lime.addPreFrameListener(updater::update)
            }
        }
    }

    private fun update(delta: Double) {
        if (noDevice)
            return

        with(activeSources.iterator()) {
            while (hasNext()) {
                val source = next()

                if (source.state != AL_PLAYING) {
                    remove()
                    sources.free(source)
                }
            }
        }
    }

    private fun obtainSource(): Source? {
        val source = sources.obtain()
        if (source.handle == -1)
            return null

        activeSources += source
        return source
    }

    private fun obtainBufferHandle(): Int {
        val buffer = Buffer()
        buffers += buffer
        return buffers.size - 1
    }

    private fun getFormat(path: String): AudioFormat? {
        val extension = path.substring(path.lastIndexOf(".") + 1, path.length)
        return formats[extension.toLowerCase()]
    }

    internal fun loadSound(sound: Sound) {
        if (noDevice)
            return

        val audioFormat = getFormat(sound.path) ?: return

        FileUtils.openAssetStream(sound.path).use {
            audioFormat.createStream(it).use { stream ->
                val format = when (stream.sampleSize) {
                    8 -> when (stream.channels) {
                        1 -> AL_FORMAT_MONO8
                        2 -> AL_FORMAT_STEREO8
                        else -> -2
                    }
                    16 -> when (stream.channels) {
                        1 -> AL_FORMAT_MONO16
                        2 -> AL_FORMAT_STEREO16
                        else -> -2
                    }
                    else -> -1
                }

                if (format == -1) {
                    Log.error(this::class, "Failed to load audio format, only 8bit or 16bit formats supported")
                    return
                }

                if (format == -2) {
                    Log.error(this::class, "Failed to load audio format, only 1 or 2 channels supported")
                    return
                }

                val data = ByteArray(stream.remaining)
                val count = stream.read(data)

                val buffer = obtainBufferHandle()
                requireNotNull(buffers[buffer]).setData(data, count, stream.isBigEndian, format, stream.sampleRate)
                sound.buffer = buffer
            }
        }
    }

    fun registerFormat(extension: String, format: AudioFormat) {
        formats[extension] = format
    }

    fun play(sound: Sound, volume: Float, loop: Boolean = false): AudioPlayer {
        val source = obtainSource() ?: return AudioPlayer(-1, 1.0f)
        alSourcei(source.handle, AL_BUFFER, requireNotNull(buffers[sound.buffer]).handle)
        alSourcef(source.handle, AL_GAIN, volume)
        alSourcei(source.handle, AL_LOOPING, if (loop) AL_TRUE else AL_FALSE)
        alSourcePlay(source.handle)
        return AudioPlayer(source.handle, volume)
    }

    override fun dispose() {
        if (noDevice)
            return

        sources.dispose()
        buffers.dispose()

        alcDestroyContext(context)
        alcCloseDevice(device)
    }
}