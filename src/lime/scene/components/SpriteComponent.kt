package lime.scene.components

import lime.Lime
import lime.graphics.Color
import lime.graphics.ImageTexture
import lime.scene.Component
import java.io.DataInput
import java.io.DataOutput

class SpriteComponent : Component() {
    var path = ""
    var color = Color(Color.WHITE)
    var views = arrayOf("")

    val texture: ImageTexture? get() = Lime.assets[path]

    override fun read(input: DataInput) {
        path = input.readUTF()
        color.r = input.readFloat()
        color.g = input.readFloat()
        color.b = input.readFloat()
        color.a = input.readFloat()
        views = Array(input.readInt()) {
            input.readUTF()
        }
    }

    override fun write(output: DataOutput) {
        output.writeUTF(path)
        output.writeFloat(color.r)
        output.writeFloat(color.g)
        output.writeFloat(color.b)
        output.writeFloat(color.a)
        output.writeInt(views.size)
        views.forEach(output::writeUTF)
    }
}