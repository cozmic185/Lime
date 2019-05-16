package lime.io

import org.lwjgl.BufferUtils
import java.io.*
import java.nio.ByteBuffer
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption
import kotlin.streams.toList

object FileUtils {
    fun openLocalOutputStream(path: String): OutputStream {
        val p = Paths.get(path)
        Files.createDirectories(p.parent)
        if (!Files.exists(p))
            Files.createFile(p)
        return BufferedOutputStream(Files.newOutputStream(p))
    }

    fun openLocalInputStream(path: String): InputStream? {
        return if (!exists(path)) null else BufferedInputStream(Files.newInputStream(Paths.get(path)))
    }

    fun openResourceStream(path: String): InputStream {
        val url = FileUtils::class.java.getResource("/$path") ?: throw IOException("Resource not found: $path")
        return url.openStream()
    }

    fun openAssetStream(path: String): InputStream {
        if (!exists(path))
            throw IOException("Asset not found: $path")
        return BufferedInputStream(Files.newInputStream(Paths.get(path)))
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

    fun exists(path: String) = Files.exists(Paths.get(path))
    fun isDirectory(path: String) = Files.isDirectory(Paths.get(path))
    fun isFile(path: String) = Files.isRegularFile(Paths.get(path))
    fun list(path: String) = Files.list(Paths.get(path)).map {
        val str = it.toString()
        str.substring(str.lastIndexOf(File.separatorChar) + 1, str.length)
    }.toList()
}