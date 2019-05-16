package lime.scene.io

import lime.scene.ComponentType
import lime.scene.Entity
import lime.utils.Log
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.DataOutputStream

@Suppress("UNCHECKED_CAST")
data class EntityData(val components: MutableMap<String, ByteArray> = hashMapOf()) {
    fun read(entity: Entity) {
        for ((typeName, data) in components) {
            val type = ComponentType.getOrTryRegister(typeName)
            if (type != null) {
                val input = DataInputStream(ByteArrayInputStream(data))

                if (!entity.scene.hasComponent(entity, type)) {
                    entity.edit {
                        val component = scene.addComponent(entity, type)
                        component.read(input)
                    }
                } else
                    requireNotNull(entity.scene.getComponent(entity, type)).read(input)
            } else
                Log.error(this::class, "Unable to locate ComponentType with name '$typeName', skipping loading it")
        }
    }

    fun write(entity: Entity) {
        components.clear()
        entity.forEach {
            val stream = ByteArrayOutputStream()
            val output = DataOutputStream(stream)

            it.write(output)
            components[it.type.name] = stream.toByteArray()
        }
    }
}