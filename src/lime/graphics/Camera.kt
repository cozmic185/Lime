package lime.graphics

import lime.Lime
import org.joml.Matrix4f
import org.joml.Rectanglef
import org.joml.Vector4f

class Camera(width: Int = Lime.graphics.width, height: Int = Lime.graphics.height) {
    companion object {
        private val tempPos = Vector4f()
    }

    var width = width
        set(value) {
            needsUpdate = true
            field = value
        }

    var height = height
        set(value) {
            needsUpdate = true
            field = value
        }

    var zoom = 1.0f
        set(value) {
            needsUpdate = true
            field = value
        }

    var x = 0.0f
        set(value) {
            needsUpdate = true
            field = value
        }

    var y = 0.0f
        set(value) {
            needsUpdate = true
            field = value
        }

    val view: Matrix4f
        get() {
            update()
            return _view
        }

    val projection: Matrix4f
        get() {
            update()
            return _projection
        }

    val projView: Matrix4f
        get() {
            update()
            return _projView
        }

    val invProjView: Matrix4f
        get() {
            update()
            return _invProjView
        }

    val rectangle: Rectanglef
        get() {
            update()
            return _rectangle
        }

    private val _view = Matrix4f()
    private val _projection = Matrix4f()
    private val _projView = Matrix4f()
    private val _invProjView = Matrix4f()
    private val _rectangle = Rectanglef()

    private var needsUpdate = true

    init {
        resetPosition()
    }

    private fun update() {
        if (needsUpdate) {
            val halfWidth = width * 0.5f
            val halfHeight = height * 0.5f

            _view.setLookAt(x, y, 1.0f, x, y, 0.0f, 0.0f, -1.0f, 0.0f)
            _projection.setOrtho2D(zoom * halfWidth, -zoom * halfWidth, -zoom * halfHeight, zoom * halfHeight)
            _projView.set(_projection).mul(_view)
            _invProjView.set(_projView).invert()

            tempPos.set(-1.0f, 1.0f, 0.0f, 1.0f)
            _invProjView.transform(tempPos)

            _rectangle.minX = tempPos.x
            _rectangle.minY = tempPos.y

            tempPos.set(1.0f, -1.0f, 0.0f, 1.0f)
            _invProjView.transform(tempPos)

            _rectangle.maxX = tempPos.x
            _rectangle.maxY = tempPos.y

            needsUpdate = false
        }
    }

    fun resetPosition() {
        x = zoom * width * 0.5f
        y = zoom * height * 0.5f
    }
}