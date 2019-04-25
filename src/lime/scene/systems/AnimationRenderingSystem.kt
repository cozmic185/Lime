package lime.scene.systems

import lime.Lime
import lime.scene.ComponentSet
import lime.scene.Entity
import lime.scene.IteratingSystem
import lime.scene.archetype
import lime.scene.components.AnimationComponent
import lime.scene.components.AnimationControllerComponent
import lime.scene.components.SpriteComponent
import lime.scene.components.TransformComponent

class AnimationRenderingSystem : IteratingSystem() {
    override val archetype = archetype {
        require<AnimationComponent>()
        require<AnimationControllerComponent>()
        require<TransformComponent>()
    }

    private val componentSet = object : ComponentSet() {
        val animation by component<AnimationComponent>()
        val animationController by component<AnimationControllerComponent>()
        val transform by component<TransformComponent>()
    }

    override fun processEntity(delta: Float, entity: Entity) {
        with(componentSet) {
            this.entity = entity

            val animation = this.animation
            val animationController = this.animationController
            val transform = this.transform


            if (animationController.playing)
                animationController.stateTime += delta

            val keyFrame = animation.animation.getKeyFrame(animationController.stateTime)
            animation.views.forEach {
                Lime.views[it].draw(keyFrame, transform.x, transform.y, keyFrame.image.width * transform.scaleX, keyFrame.image.height * transform.scaleY, animation.color)
            }
        }
    }
}