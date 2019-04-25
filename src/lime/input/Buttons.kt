package lime.input

interface Button {
    val ordinal: Int
}

enum class Buttons : Button {
    LEFT,
    MIDDLE,
    RIGHT
}