package lime.scene.components

import lime.Lime
import lime.graphics.Color
import lime.graphics.ImageTexture
import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component

class SpriteComponent : Component() {
    var path = ""
    var color = Color(Color.WHITE)
    var views = arrayOf("")

    val texture: ImageTexture? get() = Lime.assets[path]

    override fun read(reader: DataReader) {
        path = reader.readString()
        color.r = reader.readFloat()
        color.g = reader.readFloat()
        color.b = reader.readFloat()
        color.a = reader.readFloat()
        views = Array(reader.readInt()) {
            reader.readString()
        }
    }

    override fun write(writer: DataWriter) {
        writer.writeString(path)
        writer.writeFloat(color.r)
        writer.writeFloat(color.g)
        writer.writeFloat(color.b)
        writer.writeFloat(color.a)
        writer.writeInt(views.size)
        views.forEach(writer::writeString)
    }
}