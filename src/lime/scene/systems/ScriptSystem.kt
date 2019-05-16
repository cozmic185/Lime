package lime.scene.systems

import lime.scene.ComponentSet
import lime.scene.Entity
import lime.scene.IteratingSystem
import lime.scene.archetype
import lime.scene.components.ScriptComponent

class ScriptSystem : IteratingSystem() {
    override val archetype = archetype {
        require<ScriptComponent>()
    }

    private val componentSet = object : ComponentSet() {
        val script by component<ScriptComponent>()
    }

    override fun processEntity(delta: Double, entity: Entity) {
        componentSet.entity = entity
        componentSet.script.script?.let {
            if (!componentSet.script.isStarted) {
                it.start()
                componentSet.script.isStarted = true
            }

            it.update(delta)
        }
    }
}