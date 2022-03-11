import org.joml.*

class Transform : Transformable {
    private val matrix: Matrix4f = Matrix4f().identity()
    private var transformedFlag = false
    private val translation: Vector3f = Vector3f()
    private val rotation: Vector3f = Vector3f()
    private val scale: Vector3f = Vector3f(1f)
    private val rotQuat = Quaternionf()

    override fun getTranslation(): Vector3fc = translation

    override fun getRotation(): Vector3fc = rotQuat.getEulerAnglesXYZ(rotation)

    override fun getScale(): Vector3fc = scale

    override fun rotate(x: Float, y: Float, z: Float) {
        transformedFlag = true
        rotQuat.rotate(x, y, z)
    }

    override fun setRotate(x: Float, y: Float, z: Float) {
        transformedFlag = true
        rotQuat.rotation(x, y, z)
        rotQuat
    }

    override fun rotateAround(x: Float, y: Float, z: Float, center: Vector3fc) {
        rotatePosAround(x, y, z, center)
        rotQuat.rotate(x, y, z)
    }

    override fun rotatePosAround(x: Float, y: Float, z: Float, center: Vector3fc) {
        transformedFlag = true
        translation.sub(center).rotate(BUFFER_QUAT.rotation(x, y, z)).add(center)
    }

    override fun translate(x: Float, y: Float, z: Float) {
        transformedFlag = true
        translation.add(x, y, z)
    }

    override fun setTranslate(x: Float, y: Float, z: Float) {
        transformedFlag = true
    }

    override fun setScale(x: Float, y: Float, z: Float) {
        transformedFlag = true
        scale.set(x, y, z)
    }

    override fun scale(x: Float, y: Float, z: Float) {
        transformedFlag = true
        scale.mul(x, y, z)
    }

    override fun getTransforms(): Matrix4fc {
        return matrix
    }

    override fun transformed() = transformedFlag

    override fun applyTransforms(): Matrix4fc {
        transformedFlag = false
        matrix.identity()
        matrix.m00(matrix.m00() * scale.x)
        matrix.m11(matrix.m11() * scale.y)
        matrix.m22(matrix.m22() * scale.z)
        matrix.rotate(rotQuat).setTranslation(translation)
        return matrix
    }

    companion object {
        private val BUFFER_QUAT = Quaternionf()
    }
}

interface Transformable {

    fun getTranslation(): Vector3fc

    fun getRotation(): Vector3fc

    fun getScale(): Vector3fc

    fun rotate(x: Float = 0f, y: Float = 0f, z: Float = 0f)

    fun rotate(xyz: Vector3fc) = rotate(xyz.x(), xyz.y(), xyz.z())

    fun setRotate(x: Float = 0f, y: Float = 0f, z: Float = 0f)

    fun setRotate(xyz: Vector3fc) = rotate(xyz.x(), xyz.y(), xyz.z())

    fun rotateAround(x: Float = 0f, y: Float = 0f, z: Float = 0f, center: Vector3fc)

    fun rotateAround(xyz: Vector3fc, center: Vector3fc) = rotateAround(xyz.x(), xyz.y(), xyz.z(), center)

    fun rotatePosAround(x: Float = 0f, y: Float = 0f, z: Float = 0f, center: Vector3fc)

    fun rotatePosAround(xyz: Vector3fc, center: Vector3fc) = rotateAround(xyz.x(), xyz.y(), xyz.z(), center)

    fun translate(x: Float, y: Float, z: Float)

    fun translate(xyz: Vector3fc) = translate(xyz.x(), xyz.y(), xyz.z())

    //fun translateLocal(x: Float, y: Float, z: Float)

    //fun translateLocal(xyz: Vector3fc) = translateLocal(xyz.x(), xyz.y(), xyz.z())

    fun setTranslate(x: Float, y: Float, z: Float)

    fun setTranslate(xyz: Vector3fc) = setTranslate(xyz.x(), xyz.y(), xyz.z())

    fun setScale(x: Float, y: Float, z: Float)

    fun setScale(xyz: Vector3fc) = setScale(xyz.x(), xyz.y(), xyz.z())

    fun scale(x: Float, y: Float, z: Float)

    fun scale(factor: Float) = scale(factor, factor, factor)

    fun scale(xyz: Vector3fc) = scale(xyz.x(), xyz.y(), xyz.z())

    fun getTransforms(): Matrix4fc

    fun transformed(): Boolean

    fun applyTransforms(): Matrix4fc
}
