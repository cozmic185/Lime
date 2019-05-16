package lime.scene.components

import lime.scene.Component
import java.io.DataInput
import java.io.DataOutput

class NameComponent : Component() {
    var name = ""

    override fun read(input: DataInput) {
        name = input.readUTF()
    }

    override fun write(output: DataOutput) {
        output.writeUTF(name)
    }
}