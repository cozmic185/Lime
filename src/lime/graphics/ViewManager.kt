package lime.graphics

import lime.Lime
import lime.utils.DynamicArray

class ViewManager {
    val viewOrder = arrayListOf<String>()
    private val views = hashMapOf<String, DynamicArray<View>>()

    fun createViewsInOrder(vararg names: String) {
        names.forEach {
            createSubViews(it)
        }
    }

    operator fun get(name: String, index: Int = 0): View {
        val subViews = views.computeIfAbsent(name, ::createSubViews)
        var subView = subViews[index]
        if (subView == null) {
            subView = View()
            subViews[index] = subView
        }
        return subView
    }

    private fun createSubViews(name: String): DynamicArray<View> {
        val subViews = DynamicArray<View>()
        viewOrder.add(name)
        return subViews
    }

    fun render() {
        viewOrder.forEach { name ->
            views[name]?.let { subViews ->
                subViews.forEach {
                    Lime.graphics.render(it)
                }
            }
        }
    }
}