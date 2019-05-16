package lime.scene

import lime.io.FileUtils
import lime.scene.io.EntityData
import lime.scene.io.SceneData
import lime.utils.Log
import java.io.DataInputStream
import java.io.DataOutputStream
import java.lang.Exception

class SceneManager {
    private val scenes = hashMapOf<String, Scene>()
    private val active = hashSetOf<String>()

    operator fun get(name: String): Scene = scenes.computeIfAbsent(name) { Scene() }

    fun setActive(name: String, active: Boolean) {
        if (active)
            this.active += name
        else
            this.active -= name
    }

    fun isActive(name: String) = name in active

    fun processAll(delta: Double) {
        scenes.forEach { (name, scene) ->
            if (isActive(name))
                scene.process(delta)
        }
    }

    fun saveScene(name: String, dir: String = "scenes", filter: (Entity) -> Boolean = { true }) {
        val scene = scenes[name] ?: return

        Log.info(this::class, "Start saving scene '$name' to directory '$dir")

        val sceneData = SceneData()
        sceneData.write(scene, filter)

        if (sceneData.entities.isEmpty()) {
            Log.info(this::class, "No entities to save in scene '$name'")
            return
        }

        for (entityData in sceneData.entities.withIndex()) {
            var j = 0
            for ((componentType, componentData) in entityData.value.components) {
                try {
                    DataOutputStream(FileUtils.openLocalOutputStream("$dir/$name/${entityData.index}/$j/component.info")).use {
                        it.writeUTF(componentType)
                    }
                } catch (e: Exception) {
                    Log.error(this::class, "Error saving Component info for $componentType ($name)")
                    continue
                }

                try {
                    DataOutputStream(FileUtils.openLocalOutputStream("$dir/$name/${entityData.index}/$j/component.data")).use {
                        it.write(componentData)
                    }
                } catch (e: Exception) {
                    Log.error(this::class, "Error saving Component data for $componentType ($name)")
                    continue
                }

                j++
            }
        }

        Log.info(this::class, "Successfully saved ${sceneData.entities.size} entities from scene '$name' to directory '$dir")
    }

    fun loadScene(name: String, dir: String = "scenes") {
        Log.info(this::class, "Start loading scene '$name' from directory '$dir")

        val scene = scenes.computeIfAbsent(name) { Scene() }
        scene.clearEntities()

        if (!FileUtils.isDirectory("$dir/$name/")) {
            Log.info(this::class, "No scene called '$name' could be found in directory '$dir")
            return
        }

        val entityDatas = arrayListOf<EntityData>()

        for (entityDir in FileUtils.list("$dir/$name")) {
            if (FileUtils.isDirectory("$dir/$name/$entityDir")) {
                val entityFiles = FileUtils.list("$dir/$name/$entityDir")
                val componentDatas = hashMapOf<String, ByteArray>()

                for (entityFile in entityFiles) {
                    if (FileUtils.isDirectory("$dir/$name/$entityDir/$entityFile")) {
                        val componentFiles = FileUtils.list("$dir/$name/$entityDir/$entityFile")
                        if ("component.info" !in componentFiles || "component.data" !in componentFiles)
                            continue

                        val type: String
                        try {
                            val componentInfoStream = FileUtils.openLocalInputStream("$dir/$name/$entityDir/$entityFile/component.info") ?: continue
                            type = DataInputStream(componentInfoStream).use {
                                it.readUTF()
                            }
                        } catch (e: Exception) {
                            Log.error(this::class, "Error reading Component info ($dir/$name/$entityDir/$entityFile/component.info):\n${e.message}")
                            continue
                        }

                        val data: ByteArray
                        try {
                            val componentDataStream = FileUtils.openLocalInputStream("$dir/$name/$entityDir/$entityFile/component.data") ?: continue
                            data = DataInputStream(componentDataStream).use {
                                it.readAllBytes()
                            }
                        } catch (e: Exception) {
                            Log.error(this::class, "Error reading Component data ($dir/$name/$entityDir/$entityFile/component.data):\n${e.message}")
                            continue
                        }

                        componentDatas[type] = data
                    }
                }

                entityDatas += EntityData(componentDatas)
            }
        }

        val sceneData = SceneData(entityDatas)
        sceneData.read(scene)

        Log.info(this::class, "Successfully loaded ${entityDatas.size} entities for scene '$name'")
    }

    fun saveAll() {
        scenes.forEach { (name, _) ->
            saveScene(name)
        }
    }

    fun loadAll(vararg names: String) {
        names.forEach {
            loadScene(it)
        }
    }
}