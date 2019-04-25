package lime.graphics

import lime.Lime
import lime.graphics.Graphics.Companion.SIZEOF_INDEX
import lime.graphics.Graphics.Companion.SIZEOF_VERTEX
import lime.graphics.fonts.GlyphLayout
import lime.graphics.paths.Path
import org.joml.Vector2f
import org.lwjgl.BufferUtils
import java.nio.ByteBuffer
import kotlin.math.max

class View(val camera: Camera = Camera()) {
    private var indices = BufferUtils.createByteBuffer(512 * SIZEOF_INDEX)
    private var vertices = BufferUtils.createByteBuffer(512 * SIZEOF_VERTEX)
    private var currentIndex = 0

    constructor(width: Int = Lime.window.width, height: Int = Lime.window.height) : this(Camera(width, height))

    fun flush(block: (ByteBuffer, ByteBuffer) -> Unit) {
        indices.limit(indices.position())
        indices.position(0)
        vertices.limit(vertices.position())
        vertices.position(0)

        block(indices, vertices)

        indices.limit(indices.capacity())
        vertices.limit(vertices.capacity())

        currentIndex = 0
    }

    fun draw(texture: Texture, x: Float, y: Float, width: Float, height: Float, color: Color = Color.WHITE) {
        ensureSize(4, 6)

        val colorBits = color.bits

        addVertex(x, y, texture.u0, texture.v0, colorBits)
        addVertex(x + width, y, texture.u1, texture.v0, colorBits)
        addVertex(x + width, y + height, texture.u1, texture.v1, colorBits)
        addVertex(x, y + height, texture.u0, texture.v1, colorBits)

        addIndex(currentIndex)
        addIndex(currentIndex + 1)
        addIndex(currentIndex + 2)
        addIndex(currentIndex)
        addIndex(currentIndex + 2)
        addIndex(currentIndex + 3)

        currentIndex += 4
    }

    fun drawFillPath(path: Path, color: Color = Color.WHITE) {
        val vertexCount = path.size
        val indexCount = (vertexCount - 2) * 3

        ensureSize(vertexCount, indexCount)

        val colorBits = color.bits

        path.points.forEachIndexed { index, point ->
            addVertex(point.x, point.y, Lime.graphics.blankU, Lime.graphics.blankV, colorBits)

            if (index >= 2) {
                addIndex(currentIndex)
                addIndex(currentIndex + index - 1)
                addIndex(currentIndex + index)
            }
        }

        currentIndex += vertexCount
    }

    fun drawStrokePath(path: Path, thickness: Float, color: Color = Color.WHITE) {
        val pointsCount = path.size
        val indexCount = pointsCount * 6
        val vertexCount = pointsCount * 2

        ensureSize(vertexCount, indexCount)

        val colorBits = color.bits

        var directionX = 0.0f
        var directionY = 0.0f

        fun computeDirection(x0: Float, y0: Float, x1: Float, y1: Float) {
            directionX = x0 - x1
            directionY = y0 - y1
            val invLength = 1.0f / Vector2f.length(directionX, directionY)
            directionX *= invLength
            directionY *= invLength
        }

        var normalX = 0.0f
        var normalY = 0.0f

        fun computeNormal(x0: Float, y0: Float, x1: Float, y1: Float): Float {
            normalX = x0 + x1
            normalY = y0 + y1
            val invLength = 1.0f / Vector2f.length(normalX, normalY)
            normalX *= invLength
            normalY *= invLength

            return thickness * 0.5f / (normalX * -y0 + normalY * x0)
        }

        repeat(pointsCount) {
            val prevPointIndex = if (it - 1 >= 0) it - 1 else pointsCount - 1
            val pointIndex = it
            val nextPointIndex = if (it + 1 == pointsCount) 0 else it + 1

            val prevPoint = requireNotNull(path.points[prevPointIndex])
            val point = requireNotNull(path.points[pointIndex])
            val nextPoint = requireNotNull(path.points[nextPointIndex])

            computeDirection(prevPoint.x, prevPoint.y, point.x, point.y)
            val toPrevX = directionX
            val toPrevY = directionY

            computeDirection(nextPoint.x, nextPoint.y, point.x, point.y)
            val toNextX = directionX
            val toNextY = directionY

            val extrudeLength = computeNormal(toPrevX, toPrevY, toNextX, toNextY)

            addVertex(point.x + normalX * extrudeLength, point.y + normalY * extrudeLength, Lime.graphics.blankU, Lime.graphics.blankV, colorBits)
            addVertex(point.x - normalX * extrudeLength, point.y - normalY * extrudeLength, Lime.graphics.blankU, Lime.graphics.blankV, colorBits)

            val prevPointDrawIndex = prevPointIndex * 2
            val pointDrawIndex = pointIndex * 2

            addIndex(currentIndex + prevPointDrawIndex)
            addIndex(currentIndex + prevPointDrawIndex + 1)
            addIndex(currentIndex + pointDrawIndex + 1)
            addIndex(currentIndex + prevPointDrawIndex)
            addIndex(currentIndex + pointDrawIndex + 1)
            addIndex(currentIndex + pointDrawIndex)
        }

        currentIndex += pointsCount * 2
    }

    fun draw(layout: GlyphLayout, x: Float, y: Float, color: Color = Color.WHITE) {
        layout.forEachRenderGlyph {
            draw(it.texture, x + it.x, y + it.y, it.width, it.height, color)
        }
    }

    private fun ensureSize(vertices: Int, indices: Int) {
        val verticesSize = vertices * SIZEOF_VERTEX
        val indicesSize = indices * SIZEOF_INDEX

        if (verticesSize > this.vertices.remaining()) {
            val newVertices = BufferUtils.createByteBuffer(max(this.vertices.capacity() * 3 / 2, this.vertices.position() + verticesSize))
            newVertices.put(this.vertices)
            this.vertices = newVertices
        }

        if (indicesSize > this.indices.remaining()) {
            val newIndices = BufferUtils.createByteBuffer(max(this.indices.capacity() * 3 / 2, this.indices.position() + indicesSize))
            newIndices.put(this.indices)
            this.indices = newIndices
        }
    }

    private fun addVertex(x: Float, y: Float, u: Float, v: Float, color: Int) {
        vertices.putFloat(x)
        vertices.putFloat(y)
        vertices.putFloat(u)
        vertices.putFloat(v)
        vertices.putInt(color)
    }

    private fun addIndex(index: Int) {
        indices.putInt(index)
    }
}