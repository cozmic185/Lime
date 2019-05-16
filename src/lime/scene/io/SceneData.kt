package lime.scene.io

import lime.scene.Entity
import lime.scene.Scene

data class SceneData(val entities: MutableList<EntityData> = arrayListOf()) {
    fun read(scene: Scene) {
        entities.forEach {
            val entity = scene.createEntity()
            it.read(entity)
        }
    }

    fun write(scene: Scene, filter: (Entity) -> Boolean = { true }) {
        entities.clear()
        scene.forEachEntity {
            if (filter(it)) {
                val data = EntityData()
                data.write(it)
                entities += data
            }
        }
    }
}