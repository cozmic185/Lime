package lime.scene

import kotlin.reflect.KProperty

abstract class ComponentSet {
    protected class ComponentGetter<T : Component>(private val set: ComponentSet, private val type: ComponentType<T>) {
        operator fun getValue(thisRef: Any, property: KProperty<*>): T {
            return requireNotNull(set.entity.scene.getComponent(set.entity, type))
        }
    }

    lateinit var entity: Entity

    protected inline fun <reified T : Component> component() = ComponentGetter(this, ComponentType.get<T>())
}