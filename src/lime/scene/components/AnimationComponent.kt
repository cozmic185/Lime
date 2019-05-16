package lime.scene.components

import lime.Lime
import lime.graphics.Animation
import lime.graphics.Color
import lime.graphics.ImageTexture
import lime.scene.Component
import java.io.DataInput
import java.io.DataOutput

class AnimationComponent : Component() {
    var keyFramePaths = arrayOf("")
    var frameDuration = 0.0f

    var color = Color(Color.WHITE)
    var views = arrayOf("")

    lateinit var animation: Animation
        private set

    override fun initialize() {
        val keyFrames: Array<ImageTexture> = Array(keyFramePaths.size) {
            requireNotNull(Lime.assets.get<ImageTexture>(keyFramePaths[it]))
        }

        animation = Animation(frameDuration, *keyFrames)
    }

    override fun read(input: DataInput) {
        keyFramePaths = Array(input.readInt()) {
            input.readUTF()
        }

        frameDuration = input.readFloat()

        color.r = input.readFloat()
        color.g = input.readFloat()
        color.b = input.readFloat()
        color.a = input.readFloat()
        views = Array(input.readInt()) {
            input.readUTF()
        }
    }

    override fun write(output: DataOutput) {
        output.writeInt(keyFramePaths.size)
        keyFramePaths.forEach(output::writeUTF)
        output.writeFloat(frameDuration)

        output.writeFloat(color.r)
        output.writeFloat(color.g)
        output.writeFloat(color.b)
        output.writeFloat(color.a)
        output.writeInt(views.size)
        views.forEach(output::writeUTF)
    }
}