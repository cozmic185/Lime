package lime.graphics

import lime.utils.Disposable
import lime.io.FileUtils
import org.lwjgl.BufferUtils
import org.lwjgl.stb.STBImage.*
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil.*
import java.nio.ByteBuffer

class Image private constructor(data: Data) {
    companion object {
        fun loadData(data: ByteBuffer): Data {
            var width = 0
            var height = 0
            lateinit var pixels: ByteBuffer

            stackPush().use {
                val pWidth = it.callocInt(1)
                val pHeight = it.callocInt(1)
                val pComp = it.callocInt(1)
                val loaded = stbi_load_from_memory(data, pWidth, pHeight, pComp, STBI_rgb_alpha)

                if (loaded == null) {
                    width = 2
                    height = 2
                    pixels = memAlloc(width * height * Int.SIZE_BYTES)
                    pixels.putInt(0, 0xFF00FFFF.toInt())
                    pixels.putInt(1, 0x000000FF)
                    pixels.putInt(2, 0xFF00FFFF.toInt())
                    pixels.putInt(3, 0x000000FF)
                } else {
                    width = pWidth.get(0)
                    height = pHeight.get(0)
                    pixels = memAlloc(width * height * Int.SIZE_BYTES)
                    memCopy(loaded, pixels)
                    stbi_image_free(loaded)
                }
            }

            return object : Data(width, height, pixels) {
                override fun dispose() {
                    memFree(pixels)
                }
            }
        }
    }

    open class Data(val width: Int, val height: Int, val pixels: ByteBuffer) : Disposable {
        override fun dispose() {}
    }

    val width: Int
    val height: Int
    val pixels: ByteBuffer

    constructor(path: String) : this(loadData(FileUtils.loadAsset(path)))

    constructor(width: Int, height: Int, pixels: ByteBuffer) : this(Data(width, height, pixels))

    internal constructor(data: ByteBuffer) : this(loadData(data))

    init {
        width = data.width
        height = data.height
        pixels = BufferUtils.createByteBuffer(data.pixels.remaining())
        memCopy(data.pixels, pixels)
        data.dispose()
    }
}