package lime.utils

/**
Modified from https://github.com/junkdog/bitvector

MIT License

Copyright (c) 2017 Adrian Papari

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
 */

@Suppress("NOTHING_TO_INLINE")
class BitField : Iterable<Int> {
    private companion object {
        const val WORD_SIZE: Int = 32

        inline fun Int.toWordIdx() = this ushr 5

        inline fun Int.bitCapacity() = this shl 5

        inline fun bitCount(bits: Int): Int {
            var i = bits
            i -= (i ushr 1 and 0x55555555)
            i = (i and 0x33333333) + (i.ushr(2) and 0x33333333)
            i += i ushr 4 and 0x0f0f0f0f
            i += i ushr 8
            i += i ushr 16
            return i and 0x3f
        }

        inline fun leadingZeros(bits: Int): Int {
            if (bits == 0)
                return 32

            var i = bits
            var n = 1

            if (i ushr 16 == 0) {
                n += 16
                i = i shl 16
            }

            if (i ushr 24 == 0) {
                n += 8
                i = i shl 8
            }

            if (i ushr 28 == 0) {
                n += 4
                i = i shl 4
            }

            if (i ushr 30 == 0) {
                n += 2
                i = i shl 2
            }

            return n - i ushr 31
        }
    }

    private var words = IntArray(1)

    constructor()

    constructor(copyFrom: BitField) {
        words = IntArray(copyFrom.words.size)
        for (i in 0 until words.size)
            words[i] = copyFrom.words[i]
    }

    fun copy() = BitField(this)

    operator fun get(index: Int): Boolean {
        val word = index.toWordIdx()
        return word < words.size && words[word] and (1 shl index) != 0
    }

    fun set(index: Int) {
        val word = index.toWordIdx()
        checkCapacity(word)
        words[word] = words[word] or (1 shl index)
    }

    operator fun set(index: Int, value: Boolean) {
        if (value)
            set(index)
        else
            clear(index)
    }

    fun unsafeGet(index: Int): Boolean {
        return words[index.toWordIdx()] and (1 shl index) != 0
    }

    fun unsafeSet(index: Int) {
        val word = index.toWordIdx()
        words[word] = words[word] or (1 shl index)
    }

    fun unsafeSet(index: Int, value: Boolean) {
        if (value)
            unsafeSet(index)
        else
            unsafeClear(index)
    }

    fun flip(index: Int) {
        val word = index.toWordIdx()
        checkCapacity(word)
        words[word] = words[word] xor (1 shl index)
    }

    fun ensureCapacity(bits: Int) {
        checkCapacity(bits.toWordIdx())
    }

    private fun checkCapacity(wordIndex: Int) {
        if (wordIndex >= words.size) {
            words = IntArray(wordIndex + 1).also { a ->
                words.forEachIndexed { idx, bits -> a[idx] = bits }
            }
        }
    }

    fun clear(index: Int) {
        val word = index.toWordIdx()
        if (word >= words.size) return
        words[word] = words[word] and (1 shl index).inv()
    }

    fun unsafeClear(index: Int) {
        val word = index.toWordIdx()
        words[word] = words[word] and (1 shl index).inv()
    }

    fun clear() {
        for (i in words.indices)
            words[i] = 0
    }

    fun fill(value: Int) {
        for (i in words.indices)
            words[i] = value
    }

    fun length(): Int {
        val bits = this.words
        for (word in bits.indices.reversed()) {
            val bitsAtWord = bits[word]
            if (bitsAtWord != 0)
                return word.bitCapacity() + WORD_SIZE - leadingZeros(bitsAtWord)
        }

        return 0
    }

    val isEmpty: Boolean
        get() = words.all { it == 0 }

    fun and(other: BitField) {
        val commonWords = minOf(words.size, other.words.size)
        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] and other.words[i]
                i++
            }
        }

        if (words.size > commonWords) {
            var i = commonWords
            val s = words.size
            while (s > i) {
                words[i] = 0
                i++
            }
        }
    }

    fun andNot(other: BitField) {
        val commonWords = minOf(words.size, other.words.size)
        var i = 0
        while (commonWords > i) {
            words[i] = words[i] and other.words[i].inv()
            i++
        }
    }

    fun or(other: BitField) {
        val commonWords = minOf(words.size, other.words.size)
        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] or other.words[i]
                i++
            }
        }

        if (commonWords < other.words.size) {
            checkCapacity(other.words.size)
            var i = commonWords
            val s = other.words.size
            while (s > i) {
                words[i] = other.words[i]
                i++
            }
        }
    }

    fun xor(other: BitField) {
        val commonWords = minOf(words.size, other.words.size)

        run {
            var i = 0
            while (commonWords > i) {
                words[i] = words[i] xor other.words[i]
                i++
            }
        }

        if (commonWords < other.words.size) {
            checkCapacity(other.words.size)
            var i = commonWords
            val s = other.words.size
            while (s > i) {
                words[i] = other.words[i]
                i++
            }
        }
    }

    fun intersects(other: BitField): Boolean {
        val bits = this.words
        val otherBits = other.words
        var i = 0
        val s = minOf(bits.size, otherBits.size)
        while (s > i) {
            if (bits[i] and otherBits[i] != 0) {
                return true
            }
            i++
        }
        return false
    }

    operator fun contains(other: BitField): Boolean {
        val bits = this.words
        val otherBits = other.words
        val otherBitsLength = otherBits.size
        val bitsLength = bits.size

        for (i in bitsLength..otherBitsLength - 1) {
            if (otherBits[i] != 0) {
                return false
            }
        }

        var i = 0
        val s = minOf(bitsLength, otherBitsLength)
        while (s > i) {
            if (bits[i] and otherBits[i] != otherBits[i]) {
                return false
            }
            i++
        }
        return true
    }

    fun cardinality(): Int {
        var count = 0
        for (i in words.indices)
            count += bitCount(words[i])

        return count
    }

    override fun iterator(): IntIterator = object : IntIterator() {
        var remaining = cardinality()
        var wordIdx = 0
        var word = if (remaining > 0) words[0] else 0

        override fun hasNext() = remaining > 0

        override fun nextInt(): Int {
            while (true) {
                if (word != 0) {
                    val t = word and -word
                    val nextBit = wordIdx.bitCapacity() + bitCount(t - 1)
                    word = word xor t

                    remaining--

                    return nextBit
                } else {
                    word = words[++wordIdx]
                }
            }
        }
    }

    override fun hashCode(): Int {
        val word = length().toWordIdx()
        var hash = 0
        var i = 0
        while (word >= i) {
            hash = 127 * hash + words[i]
            i++
        }
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (this === other)
            return true
        if (other == null)
            return false
        if (this::class != other::class)
            return false

        val field = other as BitField?
        val otherBits = field!!.words

        val commonWords = minOf(words.size, otherBits.size)
        var i = 0
        while (commonWords > i) {
            if (words[i] != otherBits[i])
                return false
            i++
        }

        if (words.size == otherBits.size)
            return true

        return length() == field.length()
    }

    override fun toString(): String {
        val cardinality = cardinality()
        val end = minOf(128, cardinality)

        if (cardinality > 0) {
            val first = "BitField[$cardinality: {" + take(128).joinToString(separator = ", ")
            val last = if (cardinality > end) " ...}]" else "}]"

            return first + last
        } else {
            return "BitField[]"
        }
    }

    fun forEachBit(f: (Int) -> Unit) {
        val w = words
        val size = w.size
        var index = 0

        while (size > index) {
            var bitset = w[index]
            while (bitset != 0) {
                val t = bitset and -bitset
                bitset = bitset xor t
                f((index shl 5) + bitCount(t - 1))
            }

            index++
        }
    }

    operator fun plusAssign(index: Int) = set(index)
    operator fun minusAssign(index: Int) = clear(index)
}