package lime.physics

import org.dyn4j.collision.Bounds
import org.dyn4j.dynamics.Capacity
import org.dyn4j.dynamics.World

fun world(capacity: Capacity? = null, bounds: Bounds? = null, block: World.() -> Unit = {}): World {
    val world = World(capacity, bounds)
    block(world)
    return world
}