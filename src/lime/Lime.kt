package lime

import lime.audio.Audio
import lime.events.EventManager
import lime.graphics.Graphics
import lime.graphics.ViewManager
import lime.input.Input
import lime.io.Assets
import lime.utils.Time
import lime.window.Window

typealias FrameListener = (Double) -> Unit

object Lime {
    enum class EngineMode {
        EDITOR,
        RUNTIME
    }

    private val shutdownListeners = arrayListOf<() -> Unit>()
    private val frameListeners = arrayListOf<FrameListener>()
    private var running = false

    lateinit var application: Application
        private set

    lateinit var input: Input
        private set

    lateinit var window: Window
        private set

    lateinit var events: EventManager
        private set

    lateinit var graphics: Graphics
        private set

    lateinit var views: ViewManager
        private set

    lateinit var audio: Audio
        private set

    lateinit var assets: Assets
        private set

    var mode = EngineMode.RUNTIME
        private set

    var deltaTime = 0.0
        private set

    inline fun inEditorMode(block: () -> Unit) {
        if (mode == EngineMode.EDITOR)
            block()
    }

    inline fun inRuntimeMode(block: () -> Unit) {
        if (mode == EngineMode.RUNTIME)
            block()
    }

    fun onShutdown(block: () -> Unit) = synchronized(shutdownListeners) {
        shutdownListeners.add(block)
    }

    fun addFrameListener(listener: FrameListener) = synchronized(frameListeners) {
        frameListeners.add(listener)
    }

    fun removeFrameListener(listener: FrameListener) = synchronized(frameListeners) {
        frameListeners.remove(listener)
    }

    fun start(application: Application, mode: EngineMode = EngineMode.RUNTIME) {
        require(!running)

        running = true

        this.application = application
        this.mode = mode

        events = EventManager()
        input = Input()
        window = createWindow(application.config)
        graphics = Graphics(application.config.adjustToContentScale)
        views = ViewManager()
        audio = Audio(application.config.audioUpdateRate)
        assets = Assets(application.config.assetsDirectory)

        application.onCreate()

        var previousTime = Time.current
        var frameCounter = 0.0

        var previousFrameTime = Time.current

        fun frame() = Time.durationOf {
            val currentFrameTime = Time.current
            deltaTime = currentFrameTime - previousFrameTime
            previousFrameTime = currentFrameTime

            window.pollEvents()
            input.update()

            synchronized(frameListeners) {
                frameListeners.forEach {
                    it(deltaTime)
                }
            }

            graphics.beginFrame()
            application.onFrame(deltaTime)
            views.render()
            graphics.endFrame()
            window.swapBuffers()
        }

        while (running && !window.isCloseRequested) {
            val currentTime = Time.current
            frameCounter += currentTime - previousTime
            previousTime = currentTime

            if (application.config.framerate <= 0)
                frameCounter -= frame()
            else {
                val frameTime = 1.0 / application.config.framerate

                if (frameCounter >= frameTime)
                    while (frameCounter >= frameTime) {
                        frame()
                        frameCounter -= frameTime
                    }
                else
                    Thread.sleep(1)
            }
        }

        application.onDispose()

        synchronized(shutdownListeners) {
            shutdownListeners.forEach {
                it()
            }
        }

        audio.dispose()
        graphics.dispose()
        window.dispose()
    }

    fun stop() {
        running = false
    }

    private fun createWindow(config: Application.Config): Window {
        val window = Window(config.title, config.width, config.height)
        window.setIcon(*config.iconPaths)
        return window
    }
}