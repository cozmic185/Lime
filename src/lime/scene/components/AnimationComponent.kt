package lime.scene.components

import lime.Lime
import lime.graphics.Animation
import lime.graphics.Color
import lime.graphics.ImageTexture
import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component

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

    override fun read(reader: DataReader) {
        keyFramePaths = Array(reader.readInt()) {
            reader.readString()
        }

        frameDuration = reader.readFloat()

        color.r = reader.readFloat()
        color.g = reader.readFloat()
        color.b = reader.readFloat()
        color.a = reader.readFloat()
        views = Array(reader.readInt()) {
            reader.readString()
        }
    }

    override fun write(writer: DataWriter) {
        writer.writeInt(keyFramePaths.size)
        keyFramePaths.forEach(writer::writeString)
        writer.writeFloat(frameDuration)

        writer.writeFloat(color.r)
        writer.writeFloat(color.g)
        writer.writeFloat(color.b)
        writer.writeFloat(color.a)
        writer.writeInt(views.size)
        views.forEach(writer::writeString)
    }
}