package lime

import lime.events.EventProcessor
import kotlin.reflect.KClass

fun config(block: Application.Config.() -> Unit = {}): Application.Config {
    val config = Application.Config()
    block(config)
    return config
}

class Application(state: State, val config: Config = config()) {
    class Config {
        var width = 1280
        var height = 720
        var adjustToContentScale = false
        var title = "Lime Game"
        var iconPaths = arrayOf("lime.png")
        var framerate = 60
        var audioUpdateRate = 10
        var assetsDirectory = "assets"
    }

    abstract class State {
        inline fun <reified E : Any> onEvent(noinline listener: (E) -> Boolean) = onEvent(E::class, listener)

        fun <E : Any> onEvent(cls: KClass<E>, listener: (E) -> Boolean) {
            Lime.application.eventProcessor.addListener(cls, listener)
        }

        open fun onCreate() {}
        abstract fun onFrame(delta: Double): State
        open fun onDispose() {}
    }

    var state = state
        private set

    private val eventProcessor by lazy {
        val processor = EventProcessor()
        Lime.events += processor
        processor
    }

    internal fun onCreate() {
        state.onCreate()
    }

    internal fun onFrame(delta: Double) {
        val newState = state.onFrame(delta)
        if (newState != state) {
            state.onDispose()
            state = newState
            state.onCreate()
        }
    }

    internal fun onDispose() {
        state.onDispose()
    }
}