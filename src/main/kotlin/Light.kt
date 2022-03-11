import org.joml.Vector3f
import org.joml.Vector3fc
import java.util.function.Consumer
import kotlin.math.cos
import kotlin.properties.Delegates
import kotlin.properties.ReadWriteProperty

val DEFAULT_DIR: Vector3fc = Vector3f(0f, 0f, -1f)
val RED: Vector3fc = Vector3f(1f, 0f, 1f)
val GREEN: Vector3fc = Vector3f(0f, 1f, 0f)
val BLUE: Vector3fc = Vector3f(0f, 0f, 1f)
val ORANGE: Vector3fc = Vector3f(1f, 0.5f, 0f)
val WHITE: Vector3fc = Vector3f(1f, 1f, 1f)


data class LightPreset(
    val color: Vector3f = Vector3f(WHITE),
    val ambient: Float = 0f,
    val diffuse: Float = 1f,
    val specular: Float = 1f,
    val intensity: Float = 1f
) {
    companion object {
        val DEFAULT = LightPreset()
    }
}

interface Light {
    val color: Vector3fc
    var intensity: Float
    var ambient: Float
    var diffuse: Float
    var specular: Float

    fun setColor(r: Float = color.x(), g: Float = color.y(), b: Float = color.z())

    fun setColor(color : Vector3f) = setColor(color.x,color.y,color.z)

    fun setTo(preset: LightPreset)
}

abstract class LightImpl(preset: LightPreset, shader: LightShader) : Light {

    private var colorMut: Vector3f by observable(preset.color, shader::updateLightColor)
    override val color: Vector3fc
        get() = colorMut
    override var intensity: Float by observable(preset.intensity, shader::updateLightIntensity)
    override var diffuse: Float by observable(preset.diffuse, shader::updateLightDiffuse)
    override var specular: Float by observable(preset.specular, shader::updateLightSpecular)

    override fun setColor(r: Float, g: Float, b: Float) {
        colorMut.set(r, g, b)
        colorMut = colorMut
    }

    override fun setTo(preset: LightPreset) {
        colorMut.set(preset.color)
        ambient = preset.ambient
        intensity = preset.intensity
        diffuse = preset.diffuse
        specular = preset.specular
    }

    override var ambient: Float by observable(preset.ambient, shader::updateLightAmbient)

    protected fun <T> observable(
        default: T, consumer: Consumer<Light>
    ): ReadWriteProperty<Any?, T> {
        return Delegates.observable(default) { _, _, _ -> consumer.accept(this) }
    }

}

interface RangedLight : Light {
    val range: Float
}

interface MovableLight : Light {
    val pos: Vector3fc

    fun setPos(x: Float = pos.x(), y: Float = pos.y(), z: Float = pos.z())

    fun setPos(pos : Vector3f) = setPos(pos.x,pos.y,pos.z)
}

interface OrientableLight : Light {
    val dir: Vector3fc

    fun setDir(x: Float = dir.x(), y: Float = dir.y(), z: Float = dir.z())

    fun setDir(dir : Vector3f) = setDir(dir.x,dir.y,dir.z)
}

class SunLight(
    preset: LightPreset,
    direction: Vector3f = Vector3f(DEFAULT_DIR),
    shader: LightShader
) : OrientableLight, LightImpl(preset, shader) {

    private var dirMut: Vector3f by observable(direction) { shader.updateLightDir(this) }
    override val dir: Vector3f
        get() = dirMut.normalize()

    override fun setDir(x: Float, y: Float, z: Float) {
        dirMut.set(x, y, z)
        dirMut = dirMut
    }
}

class PointLight(
    preset: LightPreset,
    range: Float,
    pos: Vector3f = Vector3f(),
    shader: LightShader
) : MovableLight, RangedLight, LightImpl(preset, shader) {

    private var posMut: Vector3f by observable(pos) { shader.updateLightPos(this) }
    override val pos: Vector3fc
        get() = posMut
    override val range: Float by observable(range) { shader.updateLightRange(this) }
    var linear: Float by observable(LINEAR / (range / RANGE)) {
        shader.updatePointLightLinear(this)
    }
    var quadratic: Float by observable(QUADRATIC / (range * range / (RANGE * RANGE))) {
        shader.updatePointLightQuadratic(this)
    }

    override fun setPos(x: Float, y: Float, z: Float) {
        posMut.set(x, y, z)
        posMut = posMut
    }
    companion object {
        private const val RANGE = 3250f // baseline range of the light
        private const val LINEAR = 0.007f // baseline  linear attenuation
        private const val QUADRATIC = 0.000007f // baseline quadratic attenuation
    }
}

class SpotLight(
    preset: LightPreset,
    range: Float,
    cutAngleDef: Float,
    pos: Vector3f = Vector3f(),
    dir: Vector3f = Vector3f(DEFAULT_DIR),
    shader: LightShader
) : MovableLight, OrientableLight, RangedLight, LightImpl(preset, shader) {

    private var posMut: Vector3f by observable(pos) { shader.updateLightPos(this) }
    private var dirMut: Vector3f by observable(dir) { shader.updateLightDir(this) }
    override val range: Float by observable(range) { shader.updateLightRange(this) }
    override val dir: Vector3f
        get() = dirMut.normalize()
    override val pos: Vector3fc
        get() = posMut
    var cutAngle: Float by observable(cutAngleDef) {
        val coerced = cutAngle.coerceIn(0.1f, PI)
        innerAngle = cos(coerced)
        outerAngle = cos(coerced * SMOOTH_FACTOR)
        epsilon = innerAngle - outerAngle
        shader.updateSpotLightAngle(this)
    }
    var innerAngle = cos(cutAngle)
        private set
    var outerAngle = cos(cutAngle * SMOOTH_FACTOR)
        private set
    var epsilon = innerAngle - outerAngle
        private set

    override fun setPos(x: Float, y: Float, z: Float) {
        posMut.set(x, y, z)
        posMut = posMut
    }

    override fun setDir(x: Float, y: Float, z: Float) {
        dirMut.set(x, y, z)
        dirMut = dirMut
    }

    companion object {
        const val SMOOTH_FACTOR = 1.5f
    }
}