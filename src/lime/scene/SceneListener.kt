package lime.scene

interface SceneListener {
    fun onEntityAdded(scene: Scene, entity: Entity) {}
    fun onEntityRemoved(scene: Scene, entity: Entity) {}
    fun <T : Component> onComponentAdded(scene: Scene, entity: Entity, type: ComponentType<T>) {}
    fun <T : Component> onComponentRemoved(scene: Scene, entity: Entity, type: ComponentType<T>) {}
    fun onDispose(scene: Scene) {}
}