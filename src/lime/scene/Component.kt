package lime.scene

import lime.io.DataReader
import lime.io.DataWriter

abstract class Component {
    lateinit var type: ComponentType<*>
        internal set

    open fun initialize() {}
    open fun read(reader: DataReader) {}
    open fun write(writer: DataWriter) {}
    open fun dispose() {}
}