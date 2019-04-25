package lime.utils

import lime.FrameListener

class Updater(var rate: Int, private val block: (Double) -> Unit) {
    private var accumulator = 0.0
    private val listeners = Bag<FrameListener>()

    fun update(delta: Double) {
        accumulator += delta

        val time = 1.0 / rate
        while (accumulator >= time) {
            block(time)
            listeners.forEach { it(time) }
            accumulator -= time
        }
    }

    fun addListener(listener: FrameListener) {
        listeners += listener
    }

    fun removeListener(listener: FrameListener) {
        listeners -= listener
    }
}