package lime.graphics

import lime.maths.randomInt
import kotlin.math.max
import kotlin.math.min

class Animation(var frameDuration: Float, private vararg val keyFrames: ImageTexture) {
    enum class Mode {
        NORMAL,
        REVERSED,
        LOOP,
        LOOP_REVERSED,
        LOOP_PINGPONG,
        LOOP_RANDOM
    }

    val duration = frameDuration * keyFrames.size
    var mode = Mode.NORMAL

    private var lastFrameIndex = 0
    private var lastFrameTime = 0.0f


    fun getKeyFrame(stateTime: Float): ImageTexture {
        val index = getKeyFrameIndex(stateTime)
        return keyFrames[index]
    }

    fun isFinished(stateTime: Float): Boolean {
        val index = (stateTime / frameDuration).toInt()
        return keyFrames.size - 1 < index
    }

    private fun getKeyFrameIndex(stateTime: Float): Int {
        if (keyFrames.size == 1)
            return 0

        var index = (stateTime / frameDuration).toInt()

        when (mode) {
            Mode.NORMAL -> index = min(keyFrames.lastIndex, index)
            Mode.LOOP -> index %= keyFrames.size
            Mode.LOOP_PINGPONG -> {
                index %= (keyFrames.size * 2) - 2
                if (index >= keyFrames.size)
                    index = keyFrames.size - 2 - (index - keyFrames.size)
            }
            Mode.LOOP_RANDOM -> {
                lastFrameIndex = (lastFrameTime / frameDuration).toInt()
                index = if (lastFrameIndex != index)
                    randomInt(keyFrames.size - 1)
                else
                    lastFrameIndex
            }
            Mode.REVERSED -> index = max(keyFrames.size - index - 1, 0)
            Mode.LOOP_REVERSED -> {
                index %= keyFrames.size
                index = keyFrames.size - index - 1
            }
        }

        lastFrameIndex = index
        lastFrameTime = stateTime

        return index
    }
}