import java.io.File
import java.nio.IntBuffer

const val OBJ_PATH = "models/"
const val TEST_PATH = "src/main/assets/models/"

fun objToModelData(name: String, centerPositions: Boolean = false): MeshData {
    val openGLVertices = mutableListOf<Float>()
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
        vertexData.forEach {
            val vertexIndices = VertexIndices.fromObjVertexIndices(it)
            if (!verticesIndexesMap.containsKey(vertexIndices)) {
                //if the vertex is new, add it to openGLVertices and openGLIndices
                verticesIndexesMap[vertexIndices] = uniqueVertexIndex
                openGlIndexes.add(uniqueVertexIndex)
                uniqueVertexIndex++
                val pos = posData[vertexIndices.pos]
                val tex = texturesCoordData[vertexIndices.texture]
                val nor = normalsData[vertexIndices.normal]
                openGLVertices.addAll(
                    listOf(pos[0], pos[1], pos[2], tex[0], tex[1], nor[0], nor[1], nor[2])
                )
            } else {
                //if the vertex is a duplicate, get it's index and re-add it to openGlIndices
                openGlIndexes.add(verticesIndexesMap[vertexIndices]!!)
            }
        }
    } while (++index.i < fileData.size && fileData[index.i].startsWith("f "))
    println(openGlIndexes.joinToString())
    return MeshData(openGLVertices.toFloatArray(), openGlIndexes.toIntBuffer())
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
class MeshData(val verticesData: FloatArray, val indices: IntBuffer)