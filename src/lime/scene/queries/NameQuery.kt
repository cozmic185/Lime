package lime.scene.queries

import lime.scene.Query
import lime.scene.Scene
import lime.scene.archetype
import lime.scene.components.NameComponent

class NameQuery {
    private val query = Query(archetype {
        require<NameComponent>()
    })

    fun queryForName(name: String, scene: Scene) = query.query(scene) {
        requireNotNull(it.getComponent<NameComponent>()).name == name
    }
}