import org.joml.Matrix3f
import org.joml.Matrix3fc
import org.joml.Matrix4fc
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL15.*


private val DEF_COLOR = floatArrayOf(1f, 1f, 1f, 1f)

const val MODEL_VERTEX_DATA_SIZE = 8

open class Model(
    val data: MeshData,
    val material: Material
) : Transform() {

    private val vboId = glGenBuffers()
    private val normalsMatrix = Matrix3f()

    init {
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(
            GL_ARRAY_BUFFER,
            data.verticesData,
            GL_STATIC_DRAW
        )
    }

    fun getNormalsMatrix() = normalsMatrix as Matrix3fc

    open fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
    }

    open fun render(shader: Shader, vao: Vao) {
        this.bind()
        if( transformed()) {
            super.applyTransforms()
            getTransforms().normal(normalsMatrix)
        }
        vao.initAttributes()
        shader.setUniform("model", this.getTransforms())
        shader.setUniform("worldNormals", this.getNormalsMatrix())
        this.material.bind(shader)
        glDrawElements(GL_TRIANGLES, this.data.indices)
    }
}

class Cube(
    material: Material
) : Model(CUBE_DATA, material) {
    companion object {
        val CUBE_DATA = objToModelData("cube.obj")
    }
}

class Sphere(
    material: Material
) : Model(SPHERE_DATA, material) {
    companion object {
        val SPHERE_DATA = objToModelData("sphere_flat.obj")
    }
}

data class Material(
    val texture: Texture? = null,
    val specMap: Texture? = null,
    val emissMap: Texture? = null,
    val shininess: Float = 0f
) {

    private val hasSpec = if (specMap != null) 1 else 0
    private val hasEmiss = if (emissMap != null) 1 else 0

    fun bind(shader: Shader) {
        texture?.bind()
        specMap?.bind(GL13C.GL_TEXTURE1)
        emissMap?.bind(GL13C.GL_TEXTURE2)
        shader.setUniform("material.shininess", shininess)
        shader.setUniform("material.hasSpec", hasSpec)
        shader.setUniform("material.hasEmiss", hasEmiss)
    }

    companion object {

        val woodCrate = Material(
            Texture("textures/container.png"),
            Texture("specular/container_spec.png"),
            //Texture("emissive/emissmap.png"),
            shininess = 64f
        )

        val axis = Material(
            Texture("textures/cubefaces.png"),
        )


        val whiteCube = Material(
            emissMap = Texture.WHITE
        )

        val smoothGray = Material(
            Texture("textures/gray.png"),
            Texture("textures/gray.png"),
            shininess = 32f
        )

        val none = Material(shininess = 64f)

        fun bindSamplers(shader: Shader) {
            shader.setUniform("material.texture", 0)
            shader.setUniform("material.specMap", 1)
            shader.setUniform("material.emissMap", 2)
        }

        val woodFloor = Material(
            Texture("textures/wood.png"),
            Texture("textures/wood.png"),
                    shininess = 32f
        )

    }
}