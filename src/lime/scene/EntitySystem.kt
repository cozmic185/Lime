package lime.scene

abstract class EntitySystem(val priority: Int = 0) {
    var enabled = true

    abstract fun process(delta: Float, scene: Scene)

    open fun processEntity(delta: Float, entity: Entity) {}
}

abstract class IteratingSystem(priority: Int = 0) : EntitySystem(priority) {
    abstract val archetype: Archetype

    override fun process(delta: Float, scene: Scene) {
        scene.forEachEntity(archetype) {
            processEntity(delta, it)
        }
    }
}

abstract class IntervalIteratingSystem(private val interval: Float, priority: Int = 0) : IteratingSystem(priority) {
    private var counter = 0.0f

    override fun process(delta: Float, scene: Scene) {
        counter += delta
        if (counter >= interval) {
            super.process(delta, scene)
            counter -= interval
        }
    }
}

abstract class DelayedIteratingSystem(private val delay: Float, priority: Int = 0) : IteratingSystem(priority) {
    private var counter = 0.0f

    override fun process(delta: Float, scene: Scene) {
        if (counter >= delay) {
            super.process(delta, scene)
        } else
            counter += delta
    }
}