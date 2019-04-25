package lime.io

import lime.utils.DynamicArray

class DataWriter {
    private val data = DynamicArray<Byte>(1024)

    fun getData(): ByteArray {
        val arr = ByteArray(data.size)
        data.forEachIndexed { index, value ->
            arr[index] = value
        }
        return arr
    }

    fun reset() {
        data.clear()
    }

    fun writeByte(v: Byte) {
        data.add(v)
    }

    fun writeShort(v: Short) {
        writeByte(((v.toInt() shr 8) and 0xFF).toByte())
        writeByte((v.toInt() and 0xFF).toByte())
    }

    fun writeInt(v: Int) {
        writeShort(((v shr 16) and 0xFFFF).toShort())
        writeShort((v and 0xFFFF).toShort())
    }

    fun writeLong(v: Long) {
        writeInt(((v shr 32) and 0xFFFFFFFF).toInt())
        writeInt((v and 0xFFFFFFFF).toInt())
    }

    fun writeFloat(v: Float) {
        writeInt(v.toBits())
    }

    fun writeDouble(v: Double) {
        writeLong(v.toBits())
    }

    fun writeString(v: String) {
        val count = v.length

        writeInt(count)

        var index = 0
        while (index < count) {
            val c = v[index].toInt() and 0xFF

            if (c > 127)
                break

            writeByte(c.toByte())

            index++
        }

        if (index < count) {
            while (index < count) {
                val c = v[index].toInt()

                when {
                    c <= 0x007F -> writeByte(c.toByte())
                    c > 0x07FF -> {
                        writeByte((0xE0 or ((c shr 12) and 0x0F)).toByte())
                        writeByte((0x80 or ((c shr 6) and 0x3F)).toByte())
                        writeByte((0x80 or (c and 0x3F)).toByte())
                    }
                    else -> {
                        writeByte((0xC0 or ((c shr 6) and 0x1F)).toByte())
                        writeByte((0x80 or (c and 0x3F)).toByte())
                    }
                }

                index++
            }
        }
    }
}