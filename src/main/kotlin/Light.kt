import org.joml.Vector3f
import org.joml.Vector3fc
import kotlin.math.cos

val DEFAULT_DIR: Vector3fc = Vector3f(0f, 0f, -1f)
val RED: Vector3fc = Vector3f(1f, 0f, 1f)
val GREEN: Vector3fc = Vector3f(0f, 1f, 0f)
val BLUE: Vector3fc = Vector3f(0f, 0f, 1f)
val ORANGE: Vector3fc = Vector3f(1f, 0.5f, 0f)
val WHITE: Vector3fc = Vector3f(1f, 1f, 1f)


interface Light {
    val color: Vector3f
    val ambient: Float
    val diffuse: Float
    val specular: Float

}

interface MovableLight : Light {
    val pos: Vector3f
}

interface OrientableLight : Light {
    val dir: Vector3f
}

class SunLight(
    override val color: Vector3f,
    override val ambient: Float,
    override val diffuse: Float,
    override val specular: Float,
    direction: Vector3f = Vector3f(DEFAULT_DIR)
) : OrientableLight {

    override val dir = direction
        get() = field.normalize()

}

class PointLight(
    override val color: Vector3f,
    override val ambient: Float = 0.5f,
    override val diffuse: Float = 0.2f,
    override val specular: Float = 0.2f,
    range: Float,
    override val pos: Vector3f = Vector3f(),
) : MovableLight {

    // linear / quadratic attenuation values
    // range = x, linear = y, quadratic = z
    private val ranLinQuad = parametersFromRange(range)

    val range : Float
        get() = ranLinQuad.x
    val linear : Float
        get() = ranLinQuad.y
    val quadratic : Float
        get() = ranLinQuad.z

    companion object {
        private const val baselineRange = 3250f // range of the light
        private const val baselineLinear = 0.0014f // linear attenuation
        private const val baselineQuadratic = 0.000007f // quadratic attenuation

        fun parametersFromRange(range: Float): Vector3f {
            val daddy = range / baselineRange
            val ranLinQuad = Vector3f()
            ranLinQuad.x = range
            ranLinQuad.y = baselineLinear / daddy
            ranLinQuad.z = baselineQuadratic / (daddy * daddy)
            return ranLinQuad
        }
    }
}

class SpotLight(
    override val color: Vector3f,
    override val ambient: Float = 0.1f,
    override val diffuse: Float =  0.5f,
    override val specular: Float = 0.5f,
    cutAngle: Float,
    override val pos: Vector3f = Vector3f(),
    direction: Vector3f = Vector3f(DEFAULT_DIR),
) : MovableLight, OrientableLight {

    override val dir = direction
        get() = field.normalize()

    val innerAngle = cos(cutAngle)
    val outerAngle = cos(cutAngle * SMOOTH_FACTOR)
    val epsilon = innerAngle - outerAngle

    companion object {
        const val SMOOTH_FACTOR = 1.5f
    }
}