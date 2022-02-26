import org.joml.Matrix3f
import org.joml.Matrix3fc
import org.joml.Vector3f
import org.lwjgl.opengl.GL13C
import org.lwjgl.opengl.GL15.*


private val DEF_COLOR = floatArrayOf(1f, 1f, 1f, 1f)

const val MODEL_VERTEX_DATA_SIZE = 8

open class Model(
    val data: MeshData,
    val material: Material
) : Transform() {

    private val vboId = glGenBuffers()

    init {
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(
            GL_ARRAY_BUFFER,
            data.verticesData,
            GL_STATIC_DRAW
        )
    }

    private val worldNormals = Matrix3f(matrix)
    val worldNormalsC: Matrix3fc
        get() = worldNormals

    override fun onTransform() {
        if (evenScaling()) worldNormals.set(matrix)
        else matrix.normal(worldNormals)
    }

    open fun bind() {
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
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
        val SPHERE_DATA = objToModelData("sphere.obj")
    }
}


data class Material(
    val texture: Texture? = null,
    val specMap: Texture? = null,
    val emissMap: Texture? = null,
    val emissColor: Vector3f = Vector3f(1f),
    val shininess: Float = 0f
) {

    init {
        if (specMap != null) texture!!
    }

    private val hasTexture = if (texture != null) 1 else 0
    private val hasSpec = if (specMap != null) 1 else 0
    private val hasEmiss = if (emissMap != null) 1 else 0

    fun bind(shader: Shader) {
        texture?.bind()
        specMap?.bind(GL13C.GL_TEXTURE1)
        emissMap?.bind(GL13C.GL_TEXTURE2)
        shader.setUniform("material.emissColor", emissColor)
        shader.setUniform("material.shininess", shininess)
        shader.setUniform("material.hasTexture", hasTexture)
        shader.setUniform("material.hasSpec", hasSpec)
        shader.setUniform("material.hasEmiss", hasEmiss)
    }

    companion object {

        val woodCrate = Material(
            Texture("textures/container_debug.png"),
            Texture("textures/container_spec.png"),
            shininess = 64f
        )

        val axis = Material(
            Texture("textures/cubefaces.png"),
        )


        val whiteCube = Material(
            emissMap = Texture("textures/white.png"),
        )

        val smoothGray = Material(
            Texture("textures/gray.png"),
            Texture("textures/gray.png"),
            shininess = 64f
        )

        val none = Material(shininess = 64f)

        fun bindSamplers(shader: Shader) {
            shader.setUniform("material.texture", 0)
            shader.setUniform("material.specMap", 1)
            shader.setUniform("material.emissMap", 2)
        }

    }
}