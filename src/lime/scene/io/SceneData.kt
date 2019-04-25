package lime.scene.io

import lime.scene.Entity
import lime.scene.Scene

data class SceneData(val entities: MutableList<EntityData> = arrayListOf()) {
    fun read(scene: Scene) {
        entities.forEach {
            val entity = scene.getEntityByID(it.id) ?: scene.createEntity()
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
        entities.sortBy { it.id } // Needed so that deserialization works correctly with newly created entities
    }
}