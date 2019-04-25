package lime.scene.components

import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component

class TransformComponent : Component() {
    var x = 0.0f
    var y = 0.0f
    var scaleX = 1.0f
    var scaleY = 1.0f

    override fun read(reader: DataReader) {
        x = reader.readFloat()
        y = reader.readFloat()
        scaleX = reader.readFloat()
        scaleY = reader.readFloat()
    }

    override fun write(writer: DataWriter) {
        writer.writeFloat(x)
        writer.writeFloat(y)
        writer.writeFloat(scaleX)
        writer.writeFloat(scaleY)
    }
}