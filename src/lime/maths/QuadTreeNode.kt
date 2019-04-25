package lime.maths

import lime.*
import lime.utils.Bag
import org.joml.Rectanglef

class QuadTreeNode internal constructor(private val x: Float, private val y: Float, private val size: Float, private val tree: QuadTree, private var parent: QuadTreeNode? = null, private var parentDirectionIndex: NodeIndex? = null) {
    companion object {
        private val tempRectangle = Rectanglef()
    }

    internal enum class NodeIndex {
        NODE_00,
        NODE_10,
        NODE_01,
        NODE_11
    }

    private val rectangle = Rectanglef(x - size * 0.5f, y - size * 0.5f, x + size * 0.5f, y + size * 0.5f)
    private val spatials = Bag<Spatial>()
    private var children: Array<QuadTreeNode?>? = null
    private val hasChildren get() = children.isNullOrEmpty()
    private val isRootNode get() = parent == null

    private fun hasChildNode(index: NodeIndex): Boolean {
        val children = this.children ?: return false
        return children[index.ordinal] != null
    }

    private fun getChildNode(index: NodeIndex, centerX: Float, centerY: Float): QuadTreeNode {
        if (children == null)
            children = arrayOfNulls(4)

        var childNode = requireNotNull(children)[index.ordinal]

        if (childNode == null) {
            childNode = QuadTreeNode(centerX, centerY, size * 0.5f, tree, this, index)
            requireNotNull(children)[index.ordinal] = childNode
        }

        return childNode
    }

    private fun addToParent(spatial: Spatial) {
        if (isRootNode) {
            val dirX = spatial.rectangle.centerX - x
            val dirY = spatial.rectangle.centerY - y

            val halfSize = size * 0.5f

            val parent = when {
                dirX < 0.0f && dirY < 0.0f -> {
                    val centerX = x - halfSize
                    val centerY = y - halfSize

                    parentDirectionIndex = NodeIndex.NODE_00
                    QuadTreeNode(centerX, centerY, size * 2.0f, tree)
                }
                dirX >= 0.0f && dirY < 0.0f -> {
                    val centerX = x + halfSize
                    val centerY = y - halfSize

                    parentDirectionIndex = NodeIndex.NODE_10
                    QuadTreeNode(centerX, centerY, size * 2.0f, tree)
                }
                dirX < 0.0f && dirY >= 0.0f -> {
                    val centerX = x - halfSize
                    val centerY = y + halfSize

                    parentDirectionIndex = NodeIndex.NODE_01
                    QuadTreeNode(centerX, centerY, size * 2.0f, tree)
                }
                dirX >= 0.0f && dirY >= 0.0f -> {
                    val centerX = x + halfSize
                    val centerY = y + halfSize

                    parentDirectionIndex = NodeIndex.NODE_11
                    QuadTreeNode(centerX, centerY, size * 2.0f, tree)
                }
                else -> throw IllegalStateException()
            }

            this.parent = parent
            tree.rootNode = parent
        }

        if (spatial.rectangle in requireNotNull(parent).rectangle)
            requireNotNull(parent).add(spatial)
        else
            requireNotNull(parent).addToParent(spatial)
    }

    private fun fitsInChildNode(spatial: Spatial, centerX: Float, centerY: Float): Boolean {
        val halfSize = size * 0.5f
        tempRectangle.minX = centerX - halfSize
        tempRectangle.minY = centerY - halfSize
        tempRectangle.maxX = centerX + halfSize
        tempRectangle.maxY = centerY + halfSize
        return spatial.rectangle in tempRectangle
    }

    private fun tryAddToChild(spatial: Spatial): Boolean {
        val dirX = spatial.rectangle.centerX - x
        val dirY = spatial.rectangle.centerY - y

        val halfSize = size * 0.5f

        when {
            dirX < 0.0f && dirY < 0.0f -> {
                val centerX = x - halfSize
                val centerY = y - halfSize
                if (fitsInChildNode(spatial, centerX, centerY)) {
                    getChildNode(NodeIndex.NODE_00, centerX, centerY).add(spatial)
                    return true
                }
            }
            dirX >= 0.0f && dirY < 0.0f -> {
                val centerX = x + halfSize
                val centerY = y - halfSize
                if (fitsInChildNode(spatial, centerX, centerY)) {
                    getChildNode(NodeIndex.NODE_10, centerX, centerY).add(spatial)
                    return true
                }
            }
            dirX < 0.0f && dirY >= 0.0f -> {
                val centerX = x - halfSize
                val centerY = y + halfSize
                if (fitsInChildNode(spatial, centerX, centerY)) {
                    getChildNode(NodeIndex.NODE_01, centerX, centerY).add(spatial)
                    return true
                }
            }
            dirX >= 0.0f && dirY >= 0.0f -> {
                val centerX = x + halfSize
                val centerY = y + halfSize
                if (fitsInChildNode(spatial, centerX, centerY)) {
                    getChildNode(NodeIndex.NODE_11, centerX, centerY).add(spatial)
                    return true
                }
            }
        }

        return false
    }

    private fun tryCollapse() {
        val parent = this.parent ?: return

        var canCollapse = true
        for (sibling in requireNotNull(parent.children))
            if (sibling != null && sibling != this) {
                canCollapse = false
                break
            }

        if (canCollapse)
            parent.children = null
        else
            requireNotNull(parent.children)[requireNotNull(parentDirectionIndex).ordinal] = null
    }

    fun add(spatial: Spatial) {
        if (spatial.rectangle !in rectangle) {
            addToParent(spatial)
            return
        }

        if (size > tree.minNodeSize)
            if (spatial.rectangle.width <= rectangle.halfWidth && spatial.rectangle.height <= rectangle.halfHeight)
                if (tryAddToChild(spatial))
                    return

        spatials += spatial
        spatial.node = this
    }

    fun remove(spatial: Spatial): Boolean {
        if (spatials.remove(spatial)) {
            spatial.node = null

            if (!hasChildren && spatials.isEmpty())
                tryCollapse()

            return true
        }
        return false
    }

    fun move(spatial: Spatial) {
        if (spatial.rectangle in rectangle) {
            if (tryAddToChild(spatial))
                spatials.remove(spatial)

            if (spatial !in spatials)
                spatials += spatial
        } else {
            spatials.remove(spatial)

            if (spatial.rectangle.isGlobal)
                tree.add(spatial)
            else
                addToParent(spatial)
        }
    }

    fun clear() {
        spatials.forEach {
            it.node = null
        }

        children?.forEach {
            it?.clear()
        }

        tryCollapse()
    }

    fun query(rectangle: Rectanglef, block: (Spatial) -> Unit) {
        spatials.forEach {
            if (it.rectangle in rectangle)
                block(it)
        }

        children?.forEach {
            if (it != null) {
                if (it.rectangle in rectangle)
                    it.query(rectangle, block)
            }
        }
    }

    fun forEach(block: (Spatial) -> Unit) {
        spatials.forEach(block)

        children?.forEach {
            it?.query(rectangle, block)
        }
    }
}