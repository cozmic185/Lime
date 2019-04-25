package lime.io

import org.lwjgl.BufferUtils
import java.io.*
import java.nio.ByteBuffer

object FileUtils {
    fun openResourceStream(path: String): InputStream {
        val url = FileUtils::class.java.getResource("/$path") ?: throw IOException("Resource not found: $path")
        return url.openStream()
    }

    fun openAssetStream(path: String): InputStream {
        val file = File(path)
        if (!file.exists()) throw IOException("Asset not found: $path")
        return BufferedInputStream(FileInputStream(file))
    }

    fun loadResource(path: String): ByteBuffer {
        openResourceStream(path).use {
            return loadFromStream(it)
        }
    }

    fun loadAsset(path: String): ByteBuffer {
        openAssetStream(path).use {
            return loadFromStream(it)
        }
    }

    fun loadFromStream(stream: InputStream): ByteBuffer {
        val resource = BufferUtils.createByteBuffer(stream.available())

        stream.use {
            do {
                val b = it.read()
                if (b != -1)
                    resource.put(b.toByte())
            } while (b != -1)
        }

        resource.flip()

        return resource
    }
}