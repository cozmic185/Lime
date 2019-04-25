package lime.io

import lime.Lime
import lime.audio.Sound
import lime.graphics.fonts.Font
import lime.graphics.Image
import lime.graphics.ImageTexture
import lime.utils.Log
import java.io.File
import java.lang.Exception
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class Assets(private val assetsDirectory: String) {
    private class Type<T : Any>(val cls: KClass<T>, val loader: (String) -> T)

    private inner class Container<T : Any>(private val loader: (String) -> T) {
        val assets = hashMapOf<String, T>()

        fun load(path: String) {
            try {
                val asset = loader("$assetsDirectory/$path")
                assets[path] = asset
            } catch (e: Exception) {
                Log.error(Assets::class, "Failed to load asset $path:\n${e.message}")
            }
        }

        operator fun get(path: String) = assets[path]
    }

    private val types = hashMapOf<String, Type<*>>()
    private val containers = hashMapOf<KClass<*>, Container<*>>()

    init {
        registerDefaultTypes()

        fun loadInDirectory(directory: File) {
            if (!directory.exists())
                return

            if (!directory.isDirectory)
                Log.fail(this::class, "Path $directory is not a valid directory")

            directory.list().forEach {
                val file = File(it)
                if (file.isDirectory)
                    loadInDirectory(file)
                else {
                    val extension = it.substring(it.lastIndexOf('.') + 1, it.length)
                    val type = types[extension]
                    if (type == null)
                        Log.error(this::class, "Failed to find asset type of $it")
                    else {
                        val container = containers.computeIfAbsent(type.cls) { Container(type.loader) }
                        container.load(it)
                    }
                }
            }
        }

        loadInDirectory(File(assetsDirectory))

        val imageTextures = arrayListOf<ImageTexture>()

        (containers[ImageTexture::class] as? Container<ImageTexture>)?.let { container ->
            for ((_, asset) in container.assets)
                imageTextures += asset
        }

        (containers[Font::class] as? Container<Font>)?.let { container ->
            for ((_, asset) in container.assets)
                imageTextures += asset.imageTexture
        }

        Lime.graphics.loadImageTextures(imageTextures.toTypedArray())
    }

    inline fun <reified T : Any> registerAssetType(extension: String, noinline loader: (String) -> T) {
        registerAssetType(extension, loader, T::class)
    }

    fun <T : Any> registerAssetType(extension: String, loader: (String) -> T, cls: KClass<T>) {
        types[extension] = Type(cls, loader)
    }

    fun registerDefaultTypes() {
        registerAssetType("jpg") { ImageTexture(Image(it)) }
        registerAssetType("png") { ImageTexture(Image(it)) }
        registerAssetType("tga") { ImageTexture(Image(it)) }
        registerAssetType("bmp") { ImageTexture(Image(it)) }
        registerAssetType("ttf") { Font(it) }
        registerAssetType("wav") { Sound(it) }
    }

    inline operator fun <reified T : Any> get(path: String) = get(path, T::class)

    operator fun <T : Any> get(path: String, type: KClass<T>): T? {
        val container = (containers[type] as? Container<T>) ?: return null
        return container[path]
    }
}