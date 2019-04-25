package lime

import org.joml.Rectanglef
import org.joml.Vector2f
import org.joml.Vector3f

operator fun Vector2f.component1() = x
operator fun Vector2f.component2() = y
operator fun Vector3f.component1() = x
operator fun Vector3f.component2() = y
operator fun Vector3f.component3() = z

fun toRadians(degrees: Float) = Math.toRadians(degrees.toDouble()).toFloat()
fun toDegrees(radians: Float) = Math.toDegrees(radians.toDouble()).toFloat()

val Rectanglef.width get() = maxX - minX
val Rectanglef.height get() = maxY - minY
val Rectanglef.halfWidth get() = width * 0.5f
val Rectanglef.halfHeight get() = height * 0.5f
val Rectanglef.centerX get() = minX + halfWidth
val Rectanglef.centerY get() = minY + halfHeight
operator fun Rectanglef.contains(rectangle: Rectanglef) = minX <= rectangle.minX && minY <= rectangle.minY && maxX >= rectangle.maxX && maxY >= rectangle.maxY
val Rectanglef.isGlobal get() = width == 0.0f || height == 0.0f