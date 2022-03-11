import org.joml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration

const val DEF_NEAR = 0.1F
const val MAX_UP_ANGLE = 89.9f * 2 * PI / 360
val UP: Vector3fc = Vector3f(0f, 1f, 0f)

@Suppress("MemberVisibilityCanBePrivate")

abstract class Camera(
    fov: Float,
    var renderDistance: Float,
    var aspect: Float
) {
    private val proj = Matrix4f().perspective(fov.rad(), aspect, DEF_NEAR, renderDistance)
    private val view = Matrix4f()
    val pos = Vector3f()
    val rot = Vector3f()
    val dir = Vector3f()
    val xAxis = Vector3f()
    val upAxis = Vector3f(UP)
    var sensitivity = 0.005f
    var fovDefault = fov
        private set
    var currentFov = fov

    abstract fun update(delta: Duration)

    fun getUpdatedProjection(): Matrix4fc {
        return proj.identity().perspective(currentFov.rad(), aspect, DEF_NEAR, renderDistance)
    }

    fun getUpdatedView(): Matrix4fc {
        return view.setLookAt(pos, dir.copy.add(pos), upAxis)
    }

    fun getViewProj(): Matrix4f = getUpdatedProjection().copy.mul(getUpdatedView())

    fun zoom(factor: Float) {
        currentFov = fovDefault / factor
    }

    fun unZoom() {
        currentFov = fovDefault
    }

}

class FpsCamera(
    fov: Float,
    renderDistance: Float,
    aspect: Float
) : Camera( fov,renderDistance,aspect){

    var speed = 10f
    private var speedFinal = 0f

    override fun update(delta: Duration) {
        speedFinal = (delta.inSeconds * speed).toFloat()
    }

    fun setPos(x: Float, y: Float, z: Float) {
        pos.set(x, y, z)
    }

    fun forward() {
        pos.add(dir.copy.mul(speedFinal))
    }

    fun backward() {
        pos.add(dir.copy.mul(-speedFinal))
    }

    fun left() {
        pos.add(xAxis.copy.mul(-speedFinal))
    }

    fun right() {
        pos.add(xAxis.copy.mul(speedFinal))
    }

    fun rotate(xa: Float, ya: Float, za: Float): FpsCamera {
        rot.add(xa * sensitivity, ya * sensitivity, za * sensitivity)
        rot.x = rot.x.coerceIn(-MAX_UP_ANGLE, MAX_UP_ANGLE)
        dir.x = cos(rot.y) * cos(rot.x)
        dir.y = -sin(rot.x)
        dir.z = sin(rot.y) * cos(rot.x)
        val tmp = Vector2f(dir.x, dir.z).normalize()
        xAxis.x = -tmp.y
        xAxis.z = tmp.x
        return this
    }
}

