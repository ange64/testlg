import org.joml.Vector2f
import org.joml.Vector3f
import java.io.File
import java.nio.IntBuffer

const val OBJ_PATH = "models/"
const val TEST_PATH = "src/main/assets/models/"

fun objToModelData(name: String): MeshData {
    val openGLVertices = mutableListOf<FloatArray>()
    val openGlIndexes = mutableListOf<Int>()
    val posData = mutableListOf<FloatArray>()
    val normalsData = mutableListOf<FloatArray>()
    val texturesCoordData = mutableListOf<FloatArray>()
    var uniqueVertexIndex = 0
    val verticesIndexesMap = mutableMapOf<VertexIndices, Int>()
    val fileData = File("${OBJ_PATH}${name}").readLines()
    val index = Index()

    skipLinesWithoutPrefix(fileData, "v ", index)
    parseObjFloatBlock(fileData, "v ", posData, index)
    parseObjFloatBlock(fileData, "vt", texturesCoordData, index)
    parseObjFloatBlock(fileData, "vn", normalsData, index)
    skipLinesWithoutPrefix(fileData, "f ", index)

    do {
        val vertexData = fileData[index.i].split(" ").filter { it != "" }.drop(1)
        val verticesOfTriangle = mutableListOf<FloatArray>()
        vertexData.forEachIndexed { index, it ->
            val vertexIndices = VertexIndices.fromObjVertexIndices(it)
            if (!verticesIndexesMap.containsKey(vertexIndices)) {
                //if the vertex is new, add it to openGLVertices and openGLIndices
                verticesIndexesMap[vertexIndices] = uniqueVertexIndex
                openGlIndexes.add(uniqueVertexIndex)
                uniqueVertexIndex++
                val pos = posData[vertexIndices.pos]
                val tex = texturesCoordData[vertexIndices.texture]
                val nor = normalsData[vertexIndices.normal]
                val vertex = floatArrayOf(
                    pos[0], pos[1], pos[2],
                    tex[0], tex[1],
                    nor[0], nor[1], nor[2],
                    0f, 0f, 0f, // space for tangent
                )
                openGLVertices.add(vertex)
                verticesOfTriangle.add(vertex)
            } else {
                //if the vertex is a duplicate, get it's index and re-add it to openGlIndices
                val vertexIndex = verticesIndexesMap[vertexIndices]!!
                openGlIndexes.add(vertexIndex)
                verticesOfTriangle.add(openGLVertices[vertexIndex])
            }
        }
        computeNormalMapVectors(verticesOfTriangle)
    } while (++index.i < fileData.size && fileData[index.i].startsWith("f "))

    println("model loaded")
    println(openGLVertices.size)
    return MeshData(openGLVertices.flatMap { it.toList() }.toFloatArray(), openGlIndexes.toIntBuffer())
}

private fun computeNormalMapVectors(vertices: List<FloatArray>) {
    val v1 = vertices[vertices.lastIndex - 2]
    val v2 = vertices[vertices.lastIndex - 1]
    val v3 = vertices[vertices.lastIndex]

    val edge1 = Vector3f(v2[0], v2[1], v2[2]).sub(v1[0], v1[1], v1[2])
    val edge2 = Vector3f(v3[0], v3[1], v3[2]).sub(v1[0], v1[1], v1[2])
    val deltaUV1 = Vector2f(v2[3], v2[4]).sub(v1[3], v1[4])
    val deltaUV2 = Vector2f(v3[3], v3[4]).sub(v1[3], v1[4])

    val f = 1.0f / (deltaUV1.x * deltaUV2.y - deltaUV2.x * deltaUV1.y)

    val tangent = Vector3f()
    tangent.x = f * (deltaUV2.y * edge1.x - deltaUV1.y * edge2.x);
    tangent.y = f * (deltaUV2.y * edge1.y - deltaUV1.y * edge2.y);
    tangent.z = f * (deltaUV2.y * edge1.z - deltaUV1.y * edge2.z);

    setVertexTangent(v1, 8, tangent)
    setVertexTangent(v2, 8, tangent)
    setVertexTangent(v3, 8, tangent)
}

private fun setVertexTangent(vertex: FloatArray, i: Int, vector: Vector3f) {
    vertex[i] = vector.x
    vertex[i + 1] = vector.y
    vertex[i + 2] = vector.z
}

private fun parseObjFloatBlock(
    fileData: List<String>, blockPrefix: String, outputList: MutableList<FloatArray>, indexRef: Index,
) {
    do {
        val floats = fileData[indexRef.i].split(" ").filter { it != "" }.drop(1)
        outputList.add(FloatArray(floats.size) { i -> floats[i].toFloat() })
    } while (fileData[++indexRef.i].startsWith(blockPrefix))
}

private fun skipLinesWithoutPrefix(fileData: List<String>, prefix: String, indexRef: Index) {
    while (!fileData[indexRef.i].startsWith(prefix)) {
        indexRef.i++
    }
}

/**
 * contains the indices of all data needed for a vertex
 */
private data class VertexIndices(val pos: Int, val texture: Int, val normal: Int) {
    companion object {
        fun fromObjVertexIndices(vertexIndices: String): VertexIndices {
            val indices = vertexIndices.split("/")
            return VertexIndices(
                indices[0].toInt() - 1,
                indices[1].toInt() - 1,
                indices[2].toInt() - 1
            )
        }
    }
}

private data class Index(var i: Int = 0)
data class MeshData(val verticesData: FloatArray, val indices: IntBuffer) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as MeshData
        if (!verticesData.contentEquals(other.verticesData)) return false
        if (indices != other.indices) return false
        return true
    }

    override fun hashCode(): Int {
        var result = verticesData.contentHashCode()
        result = 31 * result + indices.hashCode()
        return result
    }
}