package lime.maths

import org.joml.Rectanglef

abstract class Spatial {
    val rectangle = Rectanglef(0.0f, 0.0f, 0.0f, 0.0f)

    internal var node: QuadTreeNode? = null
}