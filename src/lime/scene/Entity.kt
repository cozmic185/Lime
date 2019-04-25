package lime.scene

import lime.utils.BitField

@Suppress("NOTHING_TO_INLINE")
open class Entity internal constructor(val scene: Scene) : Iterable<Component> {
    internal var id = 0
    internal val componentBits = BitField()
    internal var editing = false

    inline fun <reified T : Component> addComponent(noinline initializer: T.() -> Unit = {}) = scene.addComponent(this, ComponentType.get(), initializer)

    inline fun <reified T : Component> removeComponent() = scene.removeComponent(this, ComponentType.get<T>())

    inline fun <reified T : Component> getComponent() = scene.getComponent(this, ComponentType.get<T>())

    inline fun <reified T : Component> hasComponent() = scene.hasComponent(this, ComponentType.get<T>())

    inline fun edit(noinline block: Entity.() -> Unit) = scene.editEntity(this, block)

    inline fun removeFromScene() = scene.removeEntity(this)

    inline fun <reified T : Component> getRequireComponent(noinline initializer: T.() -> Unit = {}): T {
        if (!hasComponent<T>())
            edit {
                addComponent(initializer)
            }
        return requireNotNull(getComponent())
    }

    override fun iterator() = object : Iterator<Component> {
        val bitsIterator = componentBits.iterator()

        override fun hasNext() = bitsIterator.hasNext()

        override fun next() = scene.getComponent(this@Entity, bitsIterator.next())!!
    }

    override fun equals(other: Any?): Boolean {
        if (other is Entity)
            return scene == other.scene && id == other.id
        return false
    }

    override fun hashCode(): Int {
        return id
    }

    override fun toString(): String {
        return "Entity$id"
    }
}