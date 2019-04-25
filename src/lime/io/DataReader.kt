package lime.io

import lime.utils.DynamicArray

class DataReader {
    private var data: DynamicArray<Byte> = DynamicArray()
    private var index = 0

    fun setData(data: ByteArray) {
        index = 0
        this.data.clear()
        data.forEach(this.data::add)
    }

    fun readByte(): Byte {
        return requireNotNull(data[index++])
    }

    fun readShort(): Short {
        val b0 = readByte().toInt() and 0xFF
        val b1 = readByte().toInt() and 0xFF
        return ((b0 shl 8) or b1).toShort()
    }

    fun readInt(): Int {
        val s0 = readShort().toInt() and 0xFFFF
        val s1 = readShort().toInt() and 0xFFFF
        return (s0 shl 16) or s1
    }

    fun readLong(): Long {
        val i0 = readInt().toLong() and 0xFFFFFFFF
        val i1 = readInt().toLong() and 0xFFFFFFFF
        return (i0 shl 32) or i1
    }

    fun readFloat(): Float {
        return Float.fromBits(readInt())
    }

    fun readDouble(): Double {
        return Double.fromBits(readLong())
    }

    fun readString(): String {
        val count = readInt()
        if (count < 1)
            return ""

        val chars = CharArray(count)

        var index = 0
        var b = 0

        while (index < count) {
            b = readByte().toInt() and 0xFF

            if (b > 127)
                break

            chars[index] = b.toChar()
        }

        if (index < count) {
            while (true) {
                when (b shr 4) {
                    0, 1, 2, 3, 4, 5, 6, 7 -> chars[index] = b.toChar()
                    12, 13 -> chars[index] = (((b and 0x1F) shl 6) or (readByte().toInt() and 0x3F)).toChar()
                    14 -> chars[index] =
                        (((b and 0x0F) shl 12) or ((readByte().toInt() and 0x3F) shl 6) or (readByte().toInt() and 0x3F)).toChar()
                }

                if (++index >= count)
                    break

                b = readByte().toInt() and 0xFF
            }
        }

        return String(chars, 0, count)
    }
}