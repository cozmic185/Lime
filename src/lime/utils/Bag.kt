package lime.utils

@Suppress("UNCHECKED_CAST")
open class Bag<T>(initialSize: Int = 10) : MutableCollection<T>, Disposable {
    private var elements = arrayOfNulls<Any>(initialSize)
    override var size = 0
        protected set

    override fun add(element: T): Boolean {
        if (size >= elements.size)
            elements = elements.copyOf(size * 3 / 2)

        elements[size++] = element
        return true
    }

    override fun remove(element: T): Boolean {
        val index = indexOf(element)
        return if (index < 0)
            false
        else
            removeIndex(index) != null
    }

    fun removeIndex(index: Int): T? {
        if (index < 0 || index >= size)
            return null

        val element = elements[index]
        elements[index] = elements[--size]
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

    operator fun get(index: Int): T? {
        return if (index < 0 || index >= size)
            null
        else
            elements[index] as T
    }

    override operator fun contains(element: T) = indexOf(element) >= 0

    override fun clear() {
        for (i in (0 until size))
            elements[i] = null
        size = 0
    }

    override fun addAll(elements: Collection<T>): Boolean {
        elements.forEach {
            add(it)
        }
        return true
    }

    override fun removeAll(elements: Collection<T>): Boolean {
        var result = false
        elements.forEach {
            if (remove(it))
                result = true
        }
        return result
    }

    override fun retainAll(elements: Collection<T>): Boolean {
        var result = false
        with(iterator()) {
            while (hasNext()) {
                if (next() !in elements) {
                    remove()
                    result = true
                }
            }
        }
        return result
    }

    override fun containsAll(elements: Collection<T>): Boolean {
        elements.forEach {
            if (it !in this)
                return false
        }
        return true
    }

    override fun isEmpty() = size == 0

    override operator fun iterator() = object : MutableIterator<T> {
        private var index = 0
        private var valid = true

        override fun hasNext(): Boolean {
            return index < size
        }

        override fun next(): T {
            valid = true
            return get(index++)!!
        }

        override fun remove() {
            require(valid)
            valid = false
            removeIndex(--index)
        }
    }


    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}