import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.*
import org.lwjgl.opengl.GL32.*

abstract class Vao(private vararg val attributes: Attribute) {

    private val id = glGenVertexArrays()
    private val byteStride = attributes.sumOf { it.size }.FBS


    fun initAttributes() {
        var offset = 0
        attributes.forEach {
            glVertexAttribPointer(
                it.index, it.size, GL_FLOAT, false, byteStride, offset.FBSl
            )
            glEnableVertexAttribArray(it.index)
            offset += it.size
        }
    }

    fun test() {
        var offset = 0
        attributes.forEach {
            glVertexAttribPointer(
                it.index, it.size, GL_FLOAT, false, byteStride, offset.FBSl
            )
            offset += it.size
        }
    }

    fun bind() {
        glBindVertexArray(id)
    }
}

class ModelVao : Vao(Pos(), TexCoord(), Normal(), Tangent())

class TextureVao : Vao(Pos(), TexCoord())


interface Attribute {
    val index: Int
    val size: Int
}

class Pos : Attribute {
    override val index: Int = 0
    override val size: Int = 3
}

class TexCoord : Attribute {
    override val index: Int = 1
    override val size: Int = 2
}

class Normal : Attribute {
    override val index: Int = 2
    override val size: Int = 3
}

class Tangent : Attribute {
    override val index: Int = 3
    override val size: Int = 3
}

class MatrixRow : Attribute {
    override val index: Int = 3
    override val size: Int = 3
}