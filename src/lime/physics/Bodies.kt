package lime.physics

import org.dyn4j.dynamics.Body
import org.dyn4j.dynamics.World
import org.dyn4j.geometry.Convex
import org.dyn4j.geometry.Geometry

fun World.body(fixtureCount: Int = 1, block: Body.() -> Unit): Body {
    val body = Body(fixtureCount)
    addBody(body)
    block(body)
    return body
}

fun Body.fixture(density: Float = 1.0f, friction: Float = 0.2f, restitution: Float = 0.0f, block: Geometry.() -> Convex) {
    addFixture(block(Geometry()), density.toDouble(), friction.toDouble(), restitution.toDouble())
}