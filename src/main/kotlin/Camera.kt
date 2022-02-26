import org.joml.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

const val DEF_NEAR = 0.1F
const val MAX_UP_ANGLE = 89.9f * 2 * PI / 360
val UP: Vector3fc = Vector3f(0f, 1f, 0f)

@Suppress("MemberVisibilityCanBePrivate")
class Camera(
    fov: Float,
    renderDistance: Float,
    aspect: Float
) {
    private val proj = Matrix4f().perspective(fov.rad(), aspect, DEF_NEAR, renderDistance)
    private val view = Matrix4f()

    val pos = Vector3f()
    val rot = Vector3f()
    val dir = Vector3f()
    val right = Vector3f()

    var speed = 0.1f
    var sensitivity = 0.005f
    var fov = fov
        private set
    var currentFov = fov
        private set
    var renderDistance = renderDistance
        private set
    var aspect = aspect
        private set

    fun viewProj(): Matrix4fc {
        view.setLookAt(pos, dir.copy.add(pos), UP)
        return proj.copy.mul(view)
    }

    @ExperimentalTime
    fun update(delta: Duration) {
        speed = (delta.inSeconds * 10).toFloat()
    }

    fun zoom(factor: Float) {
        currentFov = fov / factor
        setFov(currentFov)
    }

    fun setPos(x: Float, y: Float, z: Float) {
        pos.set(x, y, z)
    }

    fun unZoom() {
        setFov(fov)
    }

    fun setFov(fov: Float) {
        this.currentFov = fov
        updateProjection()
    }

    fun setAspect(aspect: Float) {
        this.aspect = aspect
        updateProjection()
    }

    fun setRenderDistance(renderDistance: Float) {
        this.renderDistance = renderDistance
        updateProjection()
    }

    fun forward() {
        pos.add(dir.copy.mul(speed))
    }

    fun backward() {
        pos.add(dir.copy.mul(-speed))
    }

    fun left() {
        pos.add(right.copy.mul(-speed))
    }

    fun right() {
        pos.add(right.copy.mul(speed))
    }

    fun rotate(xa: Float, ya: Float, za: Float): Camera {
        rot.add(xa * sensitivity, ya * sensitivity, za * sensitivity)
        rot.x = rot.x.coerceIn(-MAX_UP_ANGLE, MAX_UP_ANGLE)
        dir.x = cos(rot.y) * cos(rot.x)
        dir.y = -sin(rot.x)
        dir.z = sin(rot.y) * cos(rot.x)
        val tmp = Vector2f(dir.x,dir.z).normalize()
        right.x = -tmp.y
        right.z = tmp.x
        return this
    }

    private fun updateProjection() {
        proj.identity().perspective(currentFov, aspect, DEF_NEAR, renderDistance)
    }
}

