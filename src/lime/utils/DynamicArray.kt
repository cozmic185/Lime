package lime.utils

import kotlin.math.max

@Suppress("UNCHECKED_CAST")
class DynamicArray<T>(initialSize: Int = 10) : MutableIterable<T>, Disposable {
    private var elements = arrayOfNulls<Any>(initialSize)

    var size = 0
        private set

    fun add(element: T) {
        this[size] = element
    }

    fun remove(element: T): Boolean {
        val index = indexOf(element)
        return if (index < 0)
            false
        else
            removeIndex(indexOf(element)) != null
    }

    fun removeIndex(index: Int): T? {
        if (index < 0 || index >= size)
            return null

        val element = elements[index]
        elements[index] = null
        if (element != null && index == size - 1)
            size--
        return element as T?
    }

    fun indexOf(element: T): Int {
        var i = 0
        while (i < size) {
            if (elements[i] == element)
                return i

            i++
        }
        return -1
    }

    operator fun set(index: Int, element: T) {
        if (index >= elements.size)
            elements = elements.copyOf(max(index + 1, elements.size * 3 / 2))

        elements[index] = element
        size = max(size, index + 1)
    }

    operator fun get(index: Int): T? {
        if (index < 0 || index >= size)
            return null

        return elements[index] as T?
    }

    fun clear() {
        for (i in elements.indices)
            elements[i] = null
    }

    override fun iterator() = object : MutableIterator<T> {
        private var index = 0

        override fun hasNext(): Boolean {
            if (index < size) {
                var i = index
                while (i < size)
                    if (elements[i++] != null)
                        return true
            }
            return false
        }

        override fun next(): T {
            while (index < size) {
                val element = elements[index++]
                if (element != null)
                    return element as T
            }
            throw RuntimeException()
        }

        override fun remove() {
            removeIndex(index)
        }
    }


    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}