package lime.utils

import kotlin.reflect.KClass

object ReflectionUtils {
    inline fun <reified T : Any> getClassName(): String {
        val name = T::class.qualifiedName
        name ?: Log.fail(this::class, "Failed to get class name")
        return requireNotNull(name)
    }

    inline fun <reified T : Any> createInstance() = createInstance(T::class)

    fun <T : Any> createInstance(cls: KClass<T>): T {
        val ctor = cls.constructors.find { ctor ->
            ctor.parameters.isEmpty() || ctor.parameters.all {
                it.isOptional
            }
        }
        ctor ?: Log.fail(this::class, "Failed to find constructor with zero arguments or only optional arguments")

        return requireNotNull(ctor).call()
    }
}