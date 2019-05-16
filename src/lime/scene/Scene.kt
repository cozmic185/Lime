package lime.scene

import lime.scene.managers.ComponentManager
import lime.scene.managers.EntityManager
import lime.utils.Bag
import lime.utils.Disposable

class Scene : Disposable {
    private val entities = EntityManager(this)
    private val components = ComponentManager()
    private val listeners = Bag<SceneListener>()
    private val systems = ArrayList<EntitySystem>()
    private val delayedActions = ArrayList<() -> Unit>()
    private var sortSystems = true
    private var isProcessing = false

    fun addListener(listener: SceneListener) {
        listeners += listener
    }

    fun removeListener(listener: SceneListener) {
        listeners -= listener
    }

    internal fun removeAllComponents(entity: Entity) = components.removeAllComponents(entity)

    internal fun action(block: () -> Unit) {
        if (isProcessing)
            delayedActions += block
        else
            block()
    }

    fun createEntity(initializer: Entity.() -> Unit = {}): Entity {
        val entity = entities.createEntity(initializer)
        listeners.forEach {
            it.onEntityAdded(this, entity)
        }
        return entity
    }

    fun removeEntity(entity: Entity) {
        action {
            require(!entity.editing)
            entities.freeEntity(entity)
            listeners.forEach {
                it.onEntityRemoved(this, entity)
            }
        }
    }

    inline fun <reified T : Component> addComponent(entity: Entity, noinline initializer: T.() -> Unit = {}) = addComponent(entity, ComponentType.get<T>(), initializer)

    fun <T : Component> addComponent(entity: Entity, type: ComponentType<T>, initializer: T.() -> Unit = {}): T {
        require(entity.editing)
        val component = components.createComponent(entity, type, initializer)
        listeners.forEach {
            it.onComponentAdded(this, entity, type)
        }
        return component
    }

    inline fun <reified T : Component> removeComponent(entity: Entity) = removeComponent(entity, ComponentType.get<T>())

    fun <T : Component> removeComponent(entity: Entity, type: ComponentType<T>) {
        require(entity.editing)
        components.removeComponent(entity, type)
        listeners.forEach {
            it.onComponentRemoved(this, entity, type)
        }
    }

    inline fun <reified T : Component> getComponent(entity: Entity) = getComponent(entity, ComponentType.get<T>())

    fun <T : Component> getComponent(entity: Entity, type: ComponentType<T>): T? {
        return components.getComponent(entity, type)
    }

    internal fun getComponent(entity: Entity, id: Int): Component? {
        return components.getComponent(entity, id)
    }

    inline fun <reified T : Component> hasComponent(entity: Entity) = hasComponent(entity, ComponentType.get<T>())

    fun hasComponent(entity: Entity, type: ComponentType<*>): Boolean {
        return components.hasComponent(entity, type)
    }

    fun editEntity(entity: Entity, block: Entity.() -> Unit) {
        action {
            require(!entity.editing)
            entities.editEntity(entity) {
                block(entity)
            }
        }
    }

    fun forEachEntity(archetype: Archetype? = null, block: (Entity) -> Unit) = entities.forEachEntity(archetype, block)

    fun addSystem(system: EntitySystem) {
        action {
            systems += system
            sortSystems = true
        }
    }

    fun removeSystem(system: EntitySystem) {
        action {
            systems -= system
            sortSystems = true
        }
    }

    fun process(delta: Double) {
        if (sortSystems) {
            systems.sortBy { it.priority }
            sortSystems = false
        }

        isProcessing = true
        systems.forEach {
            if (it.enabled)
                it.process(delta, this)
        }
        isProcessing = false

        delayedActions.forEach {
            it()
        }
        delayedActions.clear()
    }

    fun clearEntities() {
        action {
            entities.clear()
            components.dispose()
        }
    }

    fun clearListeners() {
        action {
            listeners.clear()
        }
    }

    fun clearSystems() {
        action {
            systems.clear()
        }
    }

    override fun dispose() {
        action {
            listeners.forEach {
                it.onDispose(this)
            }

            clearEntities()
            clearListeners()
            clearSystems()
        }
    }
}