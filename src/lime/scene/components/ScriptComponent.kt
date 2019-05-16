package lime.scene.components

import lime.scene.Component
import lime.utils.Script
import java.io.DataInput
import java.io.DataOutput

class ScriptComponent : Component() {
    var script: Script? = null
    var isStarted = false

    override fun read(input: DataInput) {
        script?.read(input)
    }

    override fun write(output: DataOutput) {
        script?.write(output)
    }
}