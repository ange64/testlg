import org.lwjgl.opengl.ARBFramebufferObject.glGenerateMipmap
import org.lwjgl.opengl.GL11.*
import org.lwjgl.opengl.GL12C.GL_CLAMP_TO_EDGE
import org.lwjgl.opengl.GL13.glActiveTexture
import org.lwjgl.opengl.GL14C.GL_MIRRORED_REPEAT
import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer
import java.text.Format

@Suppress("MemberVisibilityCanBePrivate")

interface Texture {
    val textureId: Int
    val mapper : PixelFormatMapper

    fun getWidth(): Int

    fun getHeight(): Int

    fun getFormat(): PixelFormat

    fun setWrap(sAxis: Wrap, tAxis: Wrap = sAxis): Texture

    fun setFiltering(minifying: MinFilter, magnifying: MagFilter): Texture

    fun bind(activeTarget: Int)

    fun setData(width: Int, height: Int, format: PixelFormat, pixels: ByteBuffer?)

    fun dispose()

}


class TextureGl(
    private var width: Int,
    private var height: Int,
    private var format: PixelFormat,
    pixels: ByteBuffer?
) : Texture {
    override val textureId = glGenTextures()
    override val mapper = PixelFormatMapperGl()

    constructor(image: Image) : this(image.width, image.height, image.format, image.pixels)

    constructor(path: String) : this(Image(path))

    init {
        glBindTexture(GL_TEXTURE_2D, textureId)
        setWrap(Wrap.REPEAT, Wrap.REPEAT)
        setFiltering(MinFilter.LINEAR_BOTH, MagFilter.LINEAR)
        glTexImage2D(
            GL_TEXTURE_2D, 0, format.converted(mapper), width, height,
            0, format.converted(mapper), GL_UNSIGNED_BYTE, pixels
        )
        glGenerateMipmap(GL_TEXTURE_2D)
    }

    override fun setWrap(sAxis: Wrap, tAxis: Wrap): TextureGl {
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, tAxis.value)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, sAxis.value)
        return this
    }

    override fun setFiltering(minifying: MinFilter, magnifying: MagFilter): TextureGl {
        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, minifying.value)
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, magnifying.value)
        return this
    }

     override fun bind(activeTarget: Int) {
        glActiveTexture(activeTarget)
        glBindTexture(GL_TEXTURE_2D, textureId)
    }

    override fun getWidth() = width

    override fun getHeight() = height

    override fun getFormat() = format

    override fun setData(width: Int, height: Int, format: PixelFormat, pixels: ByteBuffer?) {
        this.width = width
        this.height = height
        this.format = format

        glBindTexture(GL_TEXTURE_2D, textureId)
        glTexImage2D(
            GL_TEXTURE_2D, 0, format.converted(mapper), width, height,
            0, format.converted(mapper), GL_UNSIGNED_BYTE, pixels
        )
        glGenerateMipmap(GL_TEXTURE_2D)
    }

    override fun dispose() {
        glDeleteTextures(textureId)
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

