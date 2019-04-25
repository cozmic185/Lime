package lime.utils

object ReflectionUtils {
    inline fun <reified T> getClassName(): String {
        val name = T::class.qualifiedName
        name ?: Log.fail(this::class, "Failed to get class name")
        return requireNotNull(name)
    }

    inline fun <reified T> createInstance(): T {
        val ctor = T::class.constructors.find { ctor ->
            ctor.parameters.isEmpty() || ctor.parameters.all {
                it.isOptional
            }
        }
        ctor ?: Log.fail(this::class, "Failed to find constructor with zero arguments or only optional arguments")

        return requireNotNull(ctor).call()
    }
}