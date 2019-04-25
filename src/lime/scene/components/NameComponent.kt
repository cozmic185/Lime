package lime.scene.components

import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component

class NameComponent : Component() {
    var name = ""

    override fun read(reader: DataReader) {
        name = reader.readString()
    }

    override fun write(writer: DataWriter) {
        writer.writeString(name)
    }
}