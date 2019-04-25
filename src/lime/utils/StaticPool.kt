package lime.utils

open class StaticPool<T : Any>(size: Int = 10, supplier: (Int) -> T) : Disposable {
    private var elements = Array<Any?>(size) { supplier(it) }
    private var index = elements.size

    open fun reset(element: T) {}

    fun obtain(): T? {
        return if (elements.isNotEmpty()) {
            val element = elements[--index]
            elements[index] = null
            element as T?
        } else
            null
    }

    fun free(element: T) {
        reset(element)
        elements[index++] = element
    }

    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}