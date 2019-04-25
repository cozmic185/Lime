package lime.utils

import lime.io.DataReader
import lime.io.DataWriter

interface Script {
    fun start() {}
    fun update(delta: Float) {}
    fun read(reader: DataReader) {}
    fun write(writer: DataWriter) {}
}