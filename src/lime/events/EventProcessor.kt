package lime.events

import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass

class EventProcessor {
    private val listeners = ConcurrentHashMap<KClass<*>, HashSet<(Any) -> Boolean>>()

    @Suppress("UNCHECKED_CAST")
    inline fun <reified E : Any> addListener(noinline listener: (E) -> Boolean) = addListener(E::class, listener)

    @Suppress("UNCHECKED_CAST")
    fun <E : Any> addListener(cls: KClass<E>, listener: (E) -> Boolean) {
        listeners.computeIfAbsent(cls) {
            hashSetOf()
        }.add(listener as (Any) -> Boolean)
    }

    inline operator fun <reified E : Any> plusAssign(noinline listener: (E) -> Boolean) = addListener(listener)

    fun <E : Any> process(event: E): Boolean {
        listeners[event::class]?.let {
            for (listener in it) {
                if (!listener(event))
                    return true
            }
        }
        return false
    }
}