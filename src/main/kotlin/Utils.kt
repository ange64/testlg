import org.joml.Matrix4f
import org.joml.Matrix4fc
import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.glfw.GLFW.GLFW_TRUE
import org.lwjgl.system.MemoryStack
import org.lwjgl.system.MemoryStack.stackPush
import org.lwjgl.system.MemoryUtil
import org.lwjgl.system.MemoryUtil.NULL
import java.nio.ByteBuffer
import java.nio.IntBuffer
import java.util.function.Consumer
import kotlin.math.roundToInt
import kotlin.math.sign

const val FLOAT_BYTE_SIZE = 4
const val TRUE = GLFW_TRUE
const val FALSE = GLFW_TRUE
const val PI = Math.PI.toFloat()

fun intBufferOf(vararg ints: Int): IntBuffer {
    val buffer = MemoryUtil.memAllocInt(ints.size)
    buffer.put(ints)
    buffer.flip()
    return buffer
}

fun List<Int>.toIntBuffer(): IntBuffer {
    val buffer = MemoryUtil.memAllocInt(this.size)
    this.forEach { buffer.put(it) }
    buffer.flip()
    return buffer
}

fun ByteArray.toByteBuffer() : ByteBuffer{
    val buffer = MemoryUtil.memAlloc(size)
    buffer.put(this)
    buffer.flip()
    return buffer
}

fun byteBufferOf(vararg  ints : Byte ): ByteBuffer {
    val buffer = MemoryUtil.memAlloc(ints.size)
    for(i in ints)
        buffer.put(i)
    buffer.flip()
    return buffer
}

fun ByteBuffer.free() {
    MemoryUtil.memFree(this.clear())
}

fun IntBuffer.free() {
    MemoryUtil.memFree(this.clear())
}
/**
 * Float Byte Size
 */
val Int.FBS: Int
    get() = this * FLOAT_BYTE_SIZE

/**
 * Float Byte Size as long
 */
val Int.FBSl: Long
    get() = this.toLong() * FLOAT_BYTE_SIZE

fun Long.isNull() = this == NULL

fun Float.rad() = Math.toRadians(this.toDouble()).toFloat()

fun Float.map(startRange1: Float, endRange1: Float, startRange2: Float, endRange2: Float): Float {
    val ratio = (endRange2 - startRange2) / (endRange1 - startRange1)
    return ratio * (this - startRange1) + startRange2
}

fun Float.mapOne(startRange2: Float, endRange2: Float) = map(0f, 1f, startRange2, endRange2)

fun Float.rounded() = this.roundToInt().toFloat()

fun FloatArray.copy(): FloatArray {
    return FloatArray(this.size) { this[it] }
}

val Matrix4fc.copy: Matrix4f
    get() = Matrix4f(this)
val Vector3fc.copy: Vector3f
    get() = Vector3f(this)

inline fun <R> useStack(block: (stack: MemoryStack) -> R): R {
    return stackPush().use(block)
}



