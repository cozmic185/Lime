package lime.scene

import java.io.DataInput
import java.io.DataOutput

abstract class Component {
    lateinit var type: ComponentType<*>
        internal set

    open fun initialize() {}
    open fun read(input: DataInput) {}
    open fun write(output: DataOutput) {}
    open fun dispose() {}
}