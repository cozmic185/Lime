package lime.graphics.paths

import lime.toRadians
import lime.utils.Bag
import lime.utils.Pool
import org.joml.Vector2f
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class Path {
    companion object {
        private const val DEFAULT_SEGMENT_COUNT = 9

        private val pointsPool = object : Pool<Vector2f>(supplier = { Vector2f() }) {
            override fun reset(element: Vector2f) {
                element.zero()
            }
        }
    }

    val points = Bag<Vector2f>()
    val lastPoint get() = points[points.size - 1]

    val size get() = points.size
    var minX = Float.POSITIVE_INFINITY
        private set
    var minY = Float.POSITIVE_INFINITY
        private set
    var maxX = Float.NEGATIVE_INFINITY
        private set
    var maxY = Float.NEGATIVE_INFINITY
        private set

    fun point(block: Vector2f.() -> Unit) {
        val point = pointsPool.obtain()
        block(point)

        if (point.x < minX)
            minX = point.x

        if (point.x > maxX)
            maxX = point.x

        if (point.y < minY)
            minY = point.y

        if (point.y > maxY)
            maxY = point.y

        points.add(point)
    }

    fun lineTo(x: Float, y: Float) {
        point {
            this.x = x
            this.y = y
        }
    }

    fun arcTo(x: Float, y: Float, radius: Float, angleMin: Float, angleMax: Float, segmentCount: Int = DEFAULT_SEGMENT_COUNT) {
        if (radius == 0.0f) {
            point {
                this.x = x
                this.y = y
            }
            return
        }

        repeat(segmentCount) {
            val angle = angleMin + (it.toFloat() / segmentCount) * (angleMax - angleMin)
            point {
                this.x = x + cos(angle) * radius
                this.y = y + sin(angle) * radius
            }
        }
    }

    fun bezierCurveTo(x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, segmentCount: Int = DEFAULT_SEGMENT_COUNT) {
        val x0 = lastPoint?.x ?: 0.0f
        val y0 = lastPoint?.y ?: 0.0f
        val step = 1.0f / segmentCount

        repeat(segmentCount) {
            val t = step * it
            val u = 1.0f - t
            val w0 = u * u * u
            val w1 = 3.0f * u * u * t
            val w2 = 3.0f * u * t * t
            val w3 = t * t * t

            point {
                x = w0 * x0 + w1 * x1 + w2 * x2 + w3 * x3
                y = w0 * y0 + w1 * y1 + w2 * y2 + w3 * y3
            }
        }
    }

    fun circle(x: Float, y: Float, radius: Float, segmentCount: Int = DEFAULT_SEGMENT_COUNT * 4) {
        val maxAngle = PI.toFloat() * 2.0f * ((segmentCount - 1.0f) / segmentCount.toFloat())
        arcTo(x, y, radius, 0.0f, maxAngle, segmentCount)
    }

    fun rect(x: Float, y: Float, width: Float, height: Float) {
        lineTo(x, y)
        lineTo(x + width, y)
        lineTo(x + width, y + height)
        lineTo(x, y + height)
    }

    fun roundedRect(x: Float, y: Float, width: Float, height: Float, rounding: Float, vararg corners: Corner) = roundedRect(x, y, width, height, rounding, Corners.toFlags(*corners))

    fun roundedRect(x: Float, y: Float, width: Float, height: Float, rounding: Float, roundingFlags: Int = Corners.ALL) {
        val roundingUpperLeft = if (Corners.isSet(roundingFlags, Corners.UPPER_LEFT)) rounding else 0.0f
        val roundingUpperRight = if (Corners.isSet(roundingFlags, Corners.UPPER_RIGHT)) rounding else 0.0f
        val roundingLowerLeft = if (Corners.isSet(roundingFlags, Corners.LOWER_LEFT)) rounding else 0.0f
        val roundingLowerRight = if (Corners.isSet(roundingFlags, Corners.LOWER_RIGHT)) rounding else 0.0f

        arcTo(x + roundingUpperLeft, y + roundingUpperLeft, roundingUpperLeft, toRadians(180.0f), toRadians(270.0f))
        arcTo(x + width - roundingUpperRight, y + roundingUpperRight, roundingUpperRight, toRadians(270.0f), toRadians(360.0f))
        arcTo(x + width - roundingLowerRight, y + height - roundingLowerRight, roundingLowerRight, toRadians(0.0f), toRadians(90.0f))
        arcTo(x + roundingLowerLeft, y + height - roundingLowerLeft, roundingLowerLeft, toRadians(90.0f), toRadians(180.0f))
    }

    fun reset() {
        points.forEach(pointsPool::free)
        points.clear()
        minX = Float.POSITIVE_INFINITY
        minY = Float.POSITIVE_INFINITY
        maxX = Float.NEGATIVE_INFINITY
        maxY = Float.NEGATIVE_INFINITY
    }

    operator fun contains(point: Vector2f) = contains(point.x, point.y)

    fun contains(x: Float, y: Float, onlyBounds: Boolean = false): Boolean {
        if (x < minX || y < minY || x > maxX || y > maxY)
            return false

        if (onlyBounds)
            return true

        var intersects = 0

        repeat(size) {
            val x0 = points[it]?.x ?: 0.0f
            val y0 = points[it]?.y ?: 0.0f

            val x1 = points[if (it + 1 == size) 0 else it + 1]?.x ?: 0.0f
            val y1 = points[if (it + 1 == size) 0 else it + 1]?.y ?: 0.0f

            if (((y0 <= y && y < y1) || (y1 <= y && y < y0)) && x < ((x1 - x0) / (y1 - y0) * (y - y0) + x0))
                intersects++
        }

        return (intersects and 1) == 1
    }
}