import org.joml.Vector3f
import org.joml.Vector3fc
import org.lwjgl.opengl.ARBFramebufferObject.glGenerateMipmap
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13.GL_TEXTURE0
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL13C.GL_TEXTURE1
import org.lwjgl.opengl.GL13C.GL_TEXTURE2
import org.lwjgl.opengl.GL14C.GL_MIRRORED_REPEAT
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

@Suppress("MemberVisibilityCanBePrivate")
class Texture(
    image: Image,
    filteringMin: MinFilter = MinFilter.LINEAR_BOTH,
    filteringMag: MagFilter = MagFilter.NEAREST,
    wrapU: Wrap = Wrap.REPEAT,
    wrapV: Wrap = Wrap.REPEAT,

    ) {
    constructor(
        path: String,
        filteringMin: MinFilter = MinFilter.LINEAR_BOTH,
        filteringMag: MagFilter = MagFilter.NEAREST,
        wrapU: Wrap = Wrap.REPEAT,
        wrapV: Wrap = Wrap.REPEAT
    ) : this(Image(path), filteringMin, filteringMag, wrapU, wrapV)

    val textureId: Int = glGenTextures()
    val width: Int = image.width
    val height: Int = image.height
    val format: Int = image.format

    init {
        glBindTexture(GL_TEXTURE_2D, textureId)
        setWrap(wrapU, wrapV)
        setFiltering(filteringMin, filteringMag)
        glTexImage2D(
            GL_TEXTURE_2D, 0, format, width, height,
            0, format, GL_UNSIGNED_BYTE, image.pixels
        )
        glGenerateMipmap(GL_TEXTURE_2D)
        glBindTexture(GL_TEXTURE_2D, 0)
    }

    private fun setWrap(sAxis: Wrap, tAxis: Wrap = sAxis): Texture {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, tAxis.value)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, sAxis.value)
        return this
    }

    private fun setFiltering(minifying: MinFilter, magnifying: MagFilter): Texture {
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minifying.value)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magnifying.value)
        return this
    }

    fun bind(activeTarget: Int = GL_TEXTURE0) {
        glActiveTexture(activeTarget)
        glBindTexture(GL_TEXTURE_2D, textureId)
    }

    companion object{
        val BLACK = Texture("textures/black.png")
        val WHITE = Texture("textures/white.png")
    }
}

class Image(path: String) {
    lateinit var pixels: ByteBuffer
        private set
    var format: Int = 0
        private set
    var width: Int = 0
        private set
    var height: Int = 0
        private set

    init {
        useStack {
            val wBuffer = it.mallocInt(1)
            val hBuffer = it.mallocInt(1)
            val channels = it.mallocInt(1)
            pixels = STBImage.stbi_load(path, wBuffer, hBuffer, channels, 0)!!
            format = if (channels[0] == 4) GL_RGBA else GL_RGB
            width = wBuffer[0]
            height = wBuffer[0]
        }
    }
}

enum class Wrap(val value: Int) {
    REPEAT(GL_REPEAT),
    MIRROR_REPEAT(GL_MIRRORED_REPEAT),
    CLAMP_EDGE(GL_CLAMP_TO_EDGE),
}

enum class MinFilter(val value: Int) {
    NEAREST_BOTH(GL_NEAREST_MIPMAP_NEAREST),
    NEAREST_LINEAR(GL_NEAREST_MIPMAP_LINEAR),
    LINEAR_NEAREST(GL_LINEAR_MIPMAP_NEAREST),
    LINEAR_BOTH(GL_LINEAR_MIPMAP_LINEAR),
}

enum class MagFilter(val value: Int) {
    LINEAR(GL_LINEAR),
    NEAREST(GL_NEAREST),
}

