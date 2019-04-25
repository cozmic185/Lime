package lime.scene.managers

import lime.scene.Archetype
import lime.scene.Entity
import lime.scene.Scene
import lime.utils.*

class EntityManager(private val scene: Scene) : Disposable {
    private class Family(bitField: BitField) {
        val componentBits = BitField(bitField)
        val entities = Bag<Entity>()

        fun add(entity: Entity) {
            entities += entity
        }

        fun remove(entity: Entity): Boolean {
            return entities.remove(entity)
        }
    }

    private var currentID = 0
    private val persistent = BitField()
    private val recycled = Bag<Int>()
    private val pool = Pool { Entity(scene) }
    private val families = Bag<Family>()

    private fun getFamily(bitField: BitField): Family {
        families.forEach {
            if (it.componentBits == bitField)
                return it
        }

        val family = Family(bitField)
        families += family
        return family
    }

    fun forEachEntity(archetype: Archetype? = null, block: (Entity) -> Unit) = forEachEntityInternal(archetype, block)

    private inline fun forEachEntityInternal(archetype: Archetype? = null, block: (Entity) -> Unit) {
        families.forEach {
            if (archetype != null) {
                if (archetype.matches(it.componentBits))
                    it.entities.forEach(block)
            } else
                it.entities.forEach(block)
        }
    }

    fun createEntity(initializer: Entity.() -> Unit): Entity {
        val entity = pool.obtain()
        scene.action {
            entity.id = recycled.removeIndex(0) ?: ++currentID
            entity.editing = true
            initializer(entity)
            entity.editing = false
            getFamily(entity.componentBits).add(entity)
        }

        return entity
    }

    fun editEntity(entity: Entity, block: (Entity) -> Unit) {
        if (getFamily(entity.componentBits).remove(entity)) {
            entity.editing = true
            block(entity)
            entity.editing = false
            getFamily(entity.componentBits).add(entity)
        }
    }

    fun freeEntity(entity: Entity) {
        if (getFamily(entity.componentBits).remove(entity)) {
            entity.scene.removeAllComponents(entity)
            entity.componentBits.clear()
            if (entity.id !in persistent)
                recycled.add(entity.id)
            pool.free(entity)
        } else
            Log.fail(this::class, "Failed to remove entity from it's family, it likely was removed already")
    }

    fun makePersistent(entity: Entity) {
        persistent += entity.id
    }

    fun getEntity(id: Int): Entity? {
        forEachEntityInternal {
            if (it.id == id)
                return it
        }

        return null
    }

    override fun dispose() {
        families.clear()
        recycled.clear()
        persistent.clear()
        currentID = 0
    }
}