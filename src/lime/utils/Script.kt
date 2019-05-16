package lime.utils

import java.io.DataInput
import java.io.DataOutput

interface Script {
    fun start() {}
    fun update(delta: Float) {}
    fun read(input: DataInput) {}
    fun write(output: DataOutput) {}
}