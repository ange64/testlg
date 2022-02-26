import org.joml.Vector3f
import org.junit.Test



class TestClass {

    @Test
    fun testObjParser(){
        val modelData = objToModelData("cube.obj")
        modelData.verticesData.forEach {
        }
        for(i in 0 .. modelData.verticesData.lastIndex - 7 step 8) {
            for( j in 0..7) {
                print(modelData.verticesData[i+j])
                print(", ")
            }
            println()
        }
    }
}