package lime.scene.components

import lime.scene.Component
import java.io.DataInput
import java.io.DataOutput

class TransformComponent : Component() {
    var x = 0.0f
    var y = 0.0f
    var scaleX = 1.0f
    var scaleY = 1.0f

    override fun read(input: DataInput) {
        x = input.readFloat()
        y = input.readFloat()
        scaleX = input.readFloat()
        scaleY = input.readFloat()
    }

    override fun write(output: DataOutput) {
        output.writeFloat(x)
        output.writeFloat(y)
        output.writeFloat(scaleX)
        output.writeFloat(scaleY)
    }
}