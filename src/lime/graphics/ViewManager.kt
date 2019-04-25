package lime.graphics

import lime.Lime

class ViewManager {
    val viewOrder = arrayListOf<String>()
    private val views = hashMapOf<String, View>()

    fun createViewsInOrder(vararg names: String) {
        names.forEach {
            createView(it)
        }
    }

    operator fun get(name: String) = views.computeIfAbsent(name, ::createView)

    fun createView(name: String): View {
        val view = View()
        viewOrder.add(name)
        return view
    }

    fun render() {
        viewOrder.forEach {
            views[it]?.let {
                Lime.graphics.render(it)
            }
        }
    }
}