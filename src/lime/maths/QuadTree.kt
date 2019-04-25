package lime.maths

import lime.isGlobal
import lime.utils.Bag
import org.joml.Rectanglef
import org.lwjgl.system.MathUtil.mathRoundPoT

class QuadTree(size: Float = 1024.0f * 1024.0f, x: Float = 0.0f, y: Float = 0.0f, z: Float = 0.0f, minNodeSize: Float = 16.0f) {
    internal val minNodeSize = mathRoundPoT(minNodeSize.toInt()).toFloat()
    internal var rootNode = QuadTreeNode(x, y, size, this)

    private val globalSpatials = Bag<Spatial>()

    fun add(spatial: Spatial) {
        if (spatial.rectangle.isGlobal)
            globalSpatials += spatial
        else
            rootNode.add(spatial)
    }

    fun remove(spatial: Spatial): Boolean {
        return if (spatial.rectangle.isGlobal)
            globalSpatials.remove(spatial)
        else
            requireNotNull(spatial.node).remove(spatial)
    }

    fun move(spatial: Spatial) {
        if (spatial in globalSpatials) {
            globalSpatials -= spatial
            add(spatial)
        } else
            requireNotNull(spatial.node).move(spatial)
    }

    fun clear() {
        globalSpatials.clear()
        rootNode.clear()
    }

    fun query(rectangle: Rectanglef, block: (Spatial) -> Unit) {
        globalSpatials.forEach(block)
        rootNode.query(rectangle, block)
    }

    fun forEach(block: (Spatial) -> Unit) {
        globalSpatials.forEach(block)
        rootNode.forEach(block)
    }
}