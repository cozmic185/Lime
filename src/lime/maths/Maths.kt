package lime.maths

import org.joml.Random

private val random = Random(System.currentTimeMillis())

fun <T : Comparable<T>> clamp(v: T, min: T, max: T): T = if (v < min) min else if (v > max) max else v

fun randomFloat() = random.nextFloat()

fun randomInt(range: Int) = random.nextInt(range)
