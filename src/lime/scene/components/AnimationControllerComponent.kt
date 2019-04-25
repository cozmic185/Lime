package lime.scene.components

import lime.graphics.Animation
import lime.io.DataReader
import lime.io.DataWriter
import lime.maths.clamp
import lime.scene.Component

class AnimationControllerComponent : Component() {
    var mode = Animation.Mode.NORMAL
    var stateTime = 0.0f
    var playing = false

    override fun read(reader: DataReader) {
        mode = Animation.Mode.values()[clamp(reader.readInt(), 0, Animation.Mode.values().size - 1)]
        stateTime = reader.readFloat()
        playing = reader.readByte() != 0.toByte()
    }

    override fun write(writer: DataWriter) {
        writer.writeInt(mode.ordinal)
        writer.writeFloat(stateTime)
        writer.writeByte(if (playing) 1 else 0)
    }
}