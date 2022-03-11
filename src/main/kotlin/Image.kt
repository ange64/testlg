import org.lwjgl.opengl.GL11.*

import org.lwjgl.stb.STBImage
import java.nio.ByteBuffer

data class Image(
    val width: Int,
    val height: Int,
    val format: PixelFormat,
    val pixels: ByteBuffer,
) {
    private constructor(image: Image) : this(image.width,image.height,image.format,image.pixels)
    constructor(path: String) : this(fromPath(path))
    constructor(r : Int, g: Int, b: Int) : this(
        1,1,PixelFormat.RGB,byteBufferOf(r.toByte(),g.toByte(),b.toByte())
    )

    fun freeData(){
        pixels.free()
    }

    companion object {

        fun fromPath(path: String) : Image {
            useStack {
                val wBuffer = it.mallocInt(1)
                val hBuffer = it.mallocInt(1)
                val channels = it.mallocInt(1)
                val pixels = STBImage.stbi_load(path, wBuffer, hBuffer, channels, 0)!!
                val format = if (channels[0] == 4) PixelFormat.RGBA else PixelFormat.RGB
                return Image(wBuffer[0], hBuffer[0],format,pixels)
            }
        }
    }
}


enum class PixelFormat(val id: Int) {
    RGB(0),
    RGBA(1);

    fun converted(mapper: PixelFormatMapper) : Int{
        return mapper.convertFormat(this)
    }
}

interface PixelFormatMapper{

    fun convertFormat(format: PixelFormat) : Int
}

class PixelFormatMapperGl : PixelFormatMapper {

    override fun convertFormat(format: PixelFormat): Int {
        return when (format) {
            PixelFormat.RGB -> GL_RGB
            PixelFormat.RGBA -> GL_RGBA
        }
    }
}