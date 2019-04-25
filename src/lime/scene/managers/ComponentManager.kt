package lime.scene.managers

import lime.scene.Component
import lime.scene.ComponentType
import lime.scene.Entity
import lime.utils.Disposable
import lime.utils.DynamicArray
import lime.utils.Log
import lime.utils.Pool

@Suppress("UNCHECKED_CAST")
class ComponentManager : Disposable {
    private inner class ComponentMapper<T : Component>(val type: ComponentType<T>) {
        val pool = Pool(supplier = type::createComponent)
        val components = DynamicArray<T>()

        fun createComponent(entity: Entity, initializer: T.() -> Unit): T {
            if (type.id in entity.componentBits)
                Log.fail(this::class, "$entity already has a component of the type $type")

            val component = pool.obtain()
            component.type = type
            initializer(component)
            component.initialize()
            entity.componentBits += type.id
            components[entity.id] = component
            return component
        }

        fun removeComponent(entity: Entity) {
            if (!hasComponent(entity))
                Log.fail(this::class, "$entity does not have a component of the type ${type.name}")

            val component = requireNotNull(components.removeIndex(entity.id))
            component.dispose()
            pool.free(component)
            entity.componentBits -= type.id
        }

        fun getComponent(entity: Entity): T? = if (!hasComponent(entity))
            null
        else
            components[entity.id]

        fun hasComponent(entity: Entity) = hasComponent(entity, type)
    }

    private var mappers = DynamicArray<ComponentMapper<*>>()

    private fun <T : Component> getOrCreateMapper(type: ComponentType<T>): ComponentMapper<T> {
        var mapper = mappers[type.id]
        if (mapper == null) {
            mapper = ComponentMapper(type)
            mappers[type.id] = mapper
        }
        return mapper as ComponentMapper<T>
    }

    fun <T : Component> createComponent(entity: Entity, type: ComponentType<T>, initializer: T.() -> Unit) = getOrCreateMapper(type).createComponent(entity, initializer)

    fun <T : Component> removeComponent(entity: Entity, type: ComponentType<T>) = mappers[type.id]?.removeComponent(entity)

    fun hasComponent(entity: Entity, type: ComponentType<*>) = type.id in entity.componentBits

    fun <T : Component> getComponent(entity: Entity, type: ComponentType<T>) = getComponent(entity, type.id) as? T?

    fun getComponent(entity: Entity, id: Int) = mappers[id]?.getComponent(entity)

    fun removeAllComponents(entity: Entity) {
        entity.componentBits.copy().forEachBit {
            mappers[it]?.removeComponent(entity)
        }
    }

    override fun dispose() {
        mappers.forEach { mapper ->
            mapper.components.forEach {
                it.dispose()
            }
        }
        mappers.clear()
    }
}