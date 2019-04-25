package lime.scene.components

import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component

class BoundsComponent : Component() {
    var width = 0.0f
    var height = 0.0f

    override fun read(reader: DataReader) {
        width = reader.readFloat()
        height = reader.readFloat()
    }

    override fun write(writer: DataWriter) {
        writer.writeFloat(width)
        writer.writeFloat(height)
    }
}