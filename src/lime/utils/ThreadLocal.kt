package de.cozmic.tilegameengine.util

import kotlin.reflect.KProperty

fun <T> threadLocal(initalizer: () -> T) = ThreadLocalDeletage(initalizer)

class ThreadLocalDeletage<T>(initalizer: () -> T) {
    private val threadLocal = object : ThreadLocal<T>() {
        override fun initialValue() = initalizer()
    }

    operator fun getValue(thisRef: Any?, property: KProperty<*>) = threadLocal.get()
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: T) = threadLocal.set(value)
}