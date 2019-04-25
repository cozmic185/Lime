package lime.scene

import lime.utils.ReflectionUtils

class ComponentType<T> private constructor(val name: String, private val supplier: () -> T) {
    companion object {
        private val registered = hashMapOf<String, ComponentType<*>>()
        private var currentID = 0

        inline fun <reified T : Component> register(noinline supplier: () -> T = ReflectionUtils::createInstance): ComponentType<T> = register(ReflectionUtils.getClassName<T>(), supplier)

        fun <T : Component> register(name: String, supplier: () -> T): ComponentType<T> {
            require(name !in registered)

            val type = ComponentType(name, supplier)
            type.id = ++currentID
            registered[name] = type
            return type
        }

        @Suppress("UNCHECKED_CAST")
        inline fun <reified T : Component> get(): ComponentType<T> {
            val name = ReflectionUtils.getClassName<T>()
            return (get(name)as? ComponentType<T>) ?: register()
        }

        operator fun get(name: String) = registered[name]
    }

    var id = -1

    fun createComponent() = supplier()

    override fun toString() = name
}