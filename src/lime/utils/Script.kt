package lime.utils

import java.io.DataInput
import java.io.DataOutput

interface Script {
    fun start() {}
    fun update(delta: Double) {}
    fun read(input: DataInput) {}
    fun write(output: DataOutput) {}
}