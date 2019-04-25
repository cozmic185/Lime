package lime.utils

open class Pool<T : Any>(size: Int = 10, private val supplier: () -> T) : Disposable {
    private var elements = Bag<T>(size)

    open fun reset(element: T) {}

    fun obtain(): T {
        return if (elements.isNotEmpty())
            elements.removeIndex(0) ?: supplier()
        else
            supplier()
    }

    fun free(element: T) {
        reset(element)
        elements.add(element)
    }

    override fun dispose() {
        elements.forEach {
            if (it is Disposable)
                it.dispose()
        }
    }
}