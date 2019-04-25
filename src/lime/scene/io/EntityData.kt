package lime.scene.io

import lime.io.DataReader
import lime.io.DataWriter
import lime.scene.Component
import lime.scene.ComponentType
import lime.scene.Entity
import lime.utils.Log

@Suppress("UNCHECKED_CAST")
data class EntityData(var id: Int = 0, val components: MutableMap<String, ByteArray> = hashMapOf()) {
    private val dataReader = DataReader()
    private val dataWriter = DataWriter()

    fun read(entity: Entity) {
        entity.edit {
            for ((typeName, data) in components) {
                (ComponentType[typeName] as? ComponentType<Component>)?.let {
                    if (scene.hasComponent(this, it))
                        scene.addComponent(entity, it)

                    dataReader.setData(data)
                    scene.getComponent(entity, it)?.read(dataReader)
                } ?: Log.error(this::class, "Unable to locate ComponentType with name '$typeName', skipping loading it")
            }
        }
    }

    fun write(entity: Entity) {
        components.clear()
        id = entity.id
        entity.scene.makePersistent(entity) // Needed so the id of the specified entity is not falsely reused for a different one
        entity.forEach {
            dataWriter.reset()
            it.write(dataWriter)
            components[it.type.name] = dataWriter.getData()
        }
    }
}