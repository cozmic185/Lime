package lime.scene

import lime.utils.BitField

fun archetype(block: Archetype.() -> Unit): Archetype {
    val archetype = Archetype()
    block(archetype)
    return archetype
}

class Archetype {
    private val required = BitField()
    private val excluded = BitField()

    fun allowAll() {
        required.fill(0xFFFFFFFF.toInt())
        excluded.clear()
    }

    inline fun <reified T : Component> require() = require(ComponentType.get<T>())

    fun <T : Component> require(type: ComponentType<T>) {
        required += type.id
    }

    inline fun <reified T : Component> exclude() = exclude(ComponentType.get<T>())

    fun <T : Component> exclude(type: ComponentType<T>) {
        excluded += type.id
    }

    fun matches(componentBits: BitField): Boolean {
        if (required !in componentBits)
            return false

        if (excluded.intersects(componentBits))
            return false

        return true
    }
}