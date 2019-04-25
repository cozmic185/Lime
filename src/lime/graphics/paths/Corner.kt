package lime.graphics.paths

interface Corner {
    val ordinal: Int
}

enum class Corners : Corner {
    UPPER_LEFT,
    LOWER_LEFT,
    UPPER_RIGHT,
    LOWER_RIGHT;

    companion object {
        val NONE = 0
        val ALL = toFlags(UPPER_LEFT, LOWER_LEFT, UPPER_RIGHT, LOWER_RIGHT)

        fun toFlags(vararg corners: Corner): Int {
            var flags = 0
            corners.forEach {
                flags = flags or (1 shl it.ordinal)
            }
            return flags
        }

        fun isSet(flags: Int, corner: Corner) = (flags and (1 shl corner.ordinal)) != 0
    }


}