package lime.utils

import java.lang.RuntimeException
import kotlin.reflect.KClass
import kotlin.system.exitProcess

object Log {
    fun info(caller: KClass<*>, message: String) {
        println("INFO (${caller.simpleName}): $message")
    }

    fun error(caller: KClass<*>, message: String) {
        println("ERROR (${caller.simpleName}): $message")
    }

    fun fail(caller: KClass<*>, message: String) {
        throw RuntimeException("FAILURE (${caller.simpleName}): $message")
    }
}