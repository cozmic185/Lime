package lime.scene

import lime.utils.Bag

class Query(var archetype: Archetype) : Iterable<Entity> {
    val entities = Bag<Entity>()

    fun query(scene: Scene, filter: (Entity) -> Boolean = { true }) {
        entities.clear()

        scene.forEachEntity(archetype) {
            if (filter(it))
                entities += it
        }
    }

    override fun iterator() = entities.iterator()
}