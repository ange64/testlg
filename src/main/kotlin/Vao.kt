import org.lwjgl.opengl.ARBVertexArrayObject.glBindVertexArray
import org.lwjgl.opengl.ARBVertexArrayObject.glGenVertexArrays
import org.lwjgl.opengl.GL20.*


abstract class Vao(stride: Int) {

    private val id = glGenVertexArrays()
    private val byteStride = stride.FBS

    abstract fun initAttributes()

    fun setAttribute(index: Int, size: Int = 0, offset: Int = 0) {
        glVertexAttribPointer(
            index, size, GL_FLOAT, false, byteStride, offset.FBSl
        )
        glEnableVertexAttribArray(index)
    }

    fun bind() {
        glBindVertexArray(id)
    }
}

class ModelVao : Vao(MODEL_VERTEX_DATA_SIZE) {

    override fun initAttributes() {
        setAttribute(0, 3)
        setAttribute(2, 2, 3)
        setAttribute(3, 3, 5)
    }

}
