package lime.scene.systems

import lime.Lime
import lime.scene.ComponentSet
import lime.scene.Entity
import lime.scene.IteratingSystem
import lime.scene.archetype
import lime.scene.components.SpriteComponent
import lime.scene.components.TransformComponent

class SpriteRenderingSystem : IteratingSystem() {
    override val archetype = archetype {
        require<SpriteComponent>()
        require<TransformComponent>()
    }

    private val componentSet = object : ComponentSet() {
        val sprite by component<SpriteComponent>()
        val transform by component<TransformComponent>()
    }

    override fun processEntity(delta: Double, entity: Entity) {
        with(componentSet) {
            this.entity = entity

            val sprite = this.sprite
            val transform = this.transform

            sprite.texture?.let { texture ->
                sprite.views.forEach {
                    Lime.views[it].draw(texture, transform.x, transform.y, texture.image.width * transform.scaleX, texture.image.height * transform.scaleY, sprite.color)
                }
            }
        }
    }
}