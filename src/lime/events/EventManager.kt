package lime.events

@Suppress("NOTHING_TO_INLINE")
class EventManager {
    private val eventProcessors = arrayListOf<EventProcessor>()

    fun addProcessor(processor: EventProcessor) = synchronized(eventProcessors) {
        eventProcessors += processor
    }

    fun removeProcessor(processor: EventProcessor) = synchronized(eventProcessors) {
        eventProcessors -= processor
    }

    inline operator fun plusAssign(processor: EventProcessor) = addProcessor(processor)

    inline operator fun minusAssign(processor: EventProcessor) = removeProcessor(processor)

    fun <E : Any> dispatchEvent(event: E): Boolean {
        synchronized(eventProcessors) {
            for (processor in eventProcessors) {
                if (!processor.process(event))
                    return true
            }
        }
        return false
    }
}