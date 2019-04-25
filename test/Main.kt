import lime.Application
import lime.Lime
import lime.audio.Sound
import lime.config
import lime.graphics.*
import lime.graphics.fonts.Font
import lime.graphics.fonts.GlyphLayout
import lime.input.Keys
import lime.scene.Scene
import lime.scene.components.SpriteComponent
import lime.scene.components.TransformComponent
import lime.scene.systems.AnimationRenderingSystem
import lime.scene.systems.ScriptSystem
import lime.scene.systems.SpriteRenderingSystem
import lime.window.FocusEvent
import org.joml.Random

fun main() {
    Lime.start(Application(object : Application.State() {
        val scene = Scene()
        var drawDebugScreen = false

        override fun onCreate() {
            onEvent { event: FocusEvent ->
                println(event.isFocused)
                true
            }

            val random = Random(0L)

            repeat(10) {
                scene.createEntity {
                    addComponent<TransformComponent> {
                        x = random.nextFloat() * Lime.graphics.width
                        y = random.nextFloat() * Lime.graphics.height

                        scaleX = 5.0f
                        scaleY = 5.0f
                    }

                    addComponent<SpriteComponent> {
                        path = "lime.png"
                        views = arrayOf("main")
                    }
                }
            }

            scene.addSystem(SpriteRenderingSystem())
            scene.addSystem(AnimationRenderingSystem())
            scene.addSystem(ScriptSystem())
        }

        override fun onFrame(delta: Double): Application.State {
            scene.process(delta.toFloat())

            if (Lime.input.isKeyTyped(Keys.KEY_F1))
                drawDebugScreen = !drawDebugScreen

            if (drawDebugScreen)
                Lime.graphics.drawDebugScreen(requireNotNull(Lime.assets["font.ttf"]))

            return this
        }
    }))
}