import org.joml.Matrix4f
import org.joml.Vector3f
import org.joml.Vector3fc

open class Transform : Transformable {
    override val matrix: Matrix4f = Matrix4f().identity()
    override val posC = Vector3f()
        get() = matrix.getTranslation(field)
    override val rotC: Vector3fc
        get() = rot
    override val sclC: Vector3fc
        get() = scl
    //buffers vectors to avoid costly retrieval operation from the matrix
    private val rot = Vector3f()
    private val scl = Vector3f()

    final override fun rotate(x: Float, y: Float, z: Float) {
        super.rotate(x, y, z)
        rot.add(x, y, z)
    }

    final override fun scale(x: Float, y: Float, z: Float) {
        super.scale(x, y, z)
        scl.mul(x, y, z)
    }

    final override fun scale(xyz: Float) {
        super.scale(xyz)
        scl.mul(xyz)
    }

    final override fun setRotate(x: Float, y: Float, z: Float) {
        super.setRotate(x, y, z)
        rot.set(x, y, z)
    }

    final override fun setScale(x: Float, y: Float, z: Float) {
        onTransform()
        scl.div(scl)
        matrix.scale(scl)
        matrix.scale(x, y, z)
        scl.set(x, y, z)
    }
}

interface Transformable {
    val matrix: Matrix4f
    val posC: Vector3fc
    val rotC: Vector3fc
    val sclC: Vector3fc

    fun rotate(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        matrix.rotateXYZ(x, y, z)
        onTransform()
    }

    fun translate(x: Float = 0f, y: Float = 0f, z: Float = 0f) {
        matrix.translate(x, y, z)
        onTransform()
    }

    fun scale(x: Float = 1f, y: Float = 1f, z: Float = 1f) {
        matrix.scale(x, y, z)
        onTransform()
    }

    fun scale(xyz : Float) {
        matrix.scale(xyz)
        onTransform()
    }

    fun setRotate(x: Float, y: Float, z: Float) {
        matrix.setRotationXYZ(x, y, z)
        onTransform()
    }

    fun setTranslate(x: Float, y: Float, z: Float) {
        matrix.setTranslation(x, y, z)
        onTransform()
    }

    fun setTranslate(xyz : Vector3fc) {
        matrix.setTranslation(xyz)
        onTransform()
    }

    fun setScale(x: Float, y: Float, z: Float) = Unit

    fun evenScaling() = sclC.x() == sclC.y() && sclC.x() == sclC.z()

    fun onTransform() = Unit
}