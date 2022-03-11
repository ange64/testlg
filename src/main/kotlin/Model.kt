import org.joml.Matrix3f
import org.lwjgl.opengl.ARBVertexArrayObject
import org.lwjgl.opengl.GL15.*
import org.lwjgl.opengl.GL30.glBindVertexArray


open class Model(
    val data: MeshData,
    val material: Material,
) : Transformable by Transform() {

    val normalsMatrix = Matrix3f()

    fun updateTransform() {
        if (transformed()) {
            applyTransforms()
            getTransforms().normal(normalsMatrix)
        }
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

abstract class ModelRenderer {

    protected val models = mutableSetOf<Model>()
    protected val texturesMap = mutableMapOf<Model, List<Texture?>>()

   // abstract fun addModelInstancied(model: Model, instances : Int)

    abstract fun addModel(model: Model)

    abstract fun removeModel(model: Model)

    abstract fun renderModels(shader: TextureShader)
}

class ModelRendererGl : ModelRenderer() {

    private val vao = ModelVao()
    private val modelToVbo = mutableMapOf<Model,Int>()

    override fun addModel(model: Model) {
        val vboId =  glGenBuffers()
        val textures = mutableListOf<Texture?>()
        glBindBuffer(GL_ARRAY_BUFFER, vboId)
        glBufferData(GL_ARRAY_BUFFER, model.data.verticesData, GL_STATIC_DRAW)
        model.material.getImages().forEach {
            textures.add(if(it == null) null else TextureGl(it))
        }
        modelToVbo[model] = vboId
        texturesMap[model] = textures
        models.add(model)
    }

    override fun removeModel(model: Model) {
        models.remove(model)
        texturesMap[model]!!.forEach {
            it?.dispose()
        }
        texturesMap.remove(model)
        glDeleteBuffers(modelToVbo[model]!!)
    }

    override fun renderModels(shader: TextureShader) {
        shader.use()
        vao.bind()
        models.forEach {
            it.updateTransform()
            shader.setUniform("model", it.getTransforms())
            shader.setUniform("worldNormals", it.normalsMatrix)
            texturesMap[it]!!.forEachIndexed { index, texture ->
                texture?.bind(GL_TEXTURE0 + index )
            }
            it.material.bind(shader)
            glBindBuffer(GL_ARRAY_BUFFER, modelToVbo[it]!!)
            vao.initAttributes()
            glDrawElements(GL_TRIANGLES, it.data.indices)
            glBindVertexArray(0)
        }
    }
}

data class Material(
    val texture: Image? = null,
    val specMap: Image? = null,
    val emissMap: Image? = null,
    val normalMap: Image? = null,
    val shininess: Float = 0f
) {

    fun getImages() = arrayOf(texture, specMap, emissMap, normalMap)

    fun bind(shader: TextureShader) {
        shader.setUniform("material.shininess", shininess)
        shader.setUniform("material.hasSpec", specMap != null)
        shader.setUniform("material.hasEmiss", emissMap != null)
        shader.setUniform("material.hasNormal", normalMap != null)
    }

    companion object {
        val woodCrate = Material(
            Image("textures/container.png"),
            Image("specular/container_spec.png"),
            shininess = 256f
        )

        val axis = Material(
            Image("textures/cubefaces.png"),
        )

        val whiteCube = Material(
            texture = Image(255, 255, 255),
            emissMap = Image(255, 255, 255)
        )

        val smoothGray = Material(
            Image("textures/gray.png"),
            Image("textures/gray.png"),
            shininess = 32f
        )

        val none = Material(shininess = 64f)

        val woodFloor = Material(
            Image("textures/wood.png"),
            Image("textures/wood.png"),
            shininess = 32f
        )

        val window = Material(
            Image("textures/window.png")
        )

        val concrete = Material(
            Image("textures/concrete_diffuse.jpg"),
            Image("specular/concrete_specular.png"),
            normalMap = Image("normal/concrete_normal.jpg"),
            shininess = 16f
        )

    }
}