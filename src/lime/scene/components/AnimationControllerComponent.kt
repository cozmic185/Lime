package lime.scene.components

import lime.graphics.Animation
import lime.maths.clamp
import lime.scene.Component
import java.io.DataInput
import java.io.DataOutput

class AnimationControllerComponent : Component() {
    var mode = Animation.Mode.NORMAL
    var stateTime = 0.0
    var playing = false

    override fun read(input: DataInput) {
        mode = Animation.Mode.values()[clamp(input.readInt(), 0, Animation.Mode.values().size - 1)]
        stateTime = input.readDouble()
        playing = input.readBoolean()
    }

    override fun write(output: DataOutput) {
        output.writeInt(mode.ordinal)
        output.writeDouble(stateTime)
        output.writeBoolean(playing)
    }
}