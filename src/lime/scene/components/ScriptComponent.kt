package lime.scene.components

import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component
import lime.utils.Script

class ScriptComponent : Component() {
    var script: Script? = null
    var isStarted = false

    override fun read(reader: DataReader) {
        script?.read(reader)
    }

    override fun write(writer: DataWriter) {
        script?.write(writer)
    }
}