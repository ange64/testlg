import org.joml.Matrix4fc
import org.joml.Vector3f
import org.lwjgl.opengl.GL11.*
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class AppIml : App() {

    private val vao = ModelVao()
    private val shader = PhongShader()
    private val models = mutableListOf<Model>()
    private val camera = Camera(90f, 100f, 16.0f / 9)

    private val light = PointLight(
        Vector3f(WHITE), range = 200f, pos = Vector3f(0f, 5f, 0f),
    )

    private val spotLight = SpotLight(
        Vector3f(0F), cutAngle = 15f.rad(),diffuse = 1f
    )

    private val lightModel = Cube(material = Material.whiteCube)

    private val rnd = Random(3453)

    override fun init() {
        camera.setPos(0f, 2f, 0f)
        //lightSource.scale(30f,30f,30f)
        genGround()
        for (i in 0..10) genBoxes(15f)
        models.add(lightModel)
        lightModel.scale(0.3f)
        lightModel.setTranslate(light.pos)
        lightModel.material.emissColor.set(light.color)
        val sphere = Sphere(Material.smoothGray)
        sphere.translate(10f,10f,10f)
        models.add(sphere)

        shader.use()
        shader.addLight(light)
        shader.addLight(spotLight)
        Material.bindSamplers(shader)
    }

    override fun tick(delta: Duration) {
        glClearColor(0f, 0.3f, 0.3f, 1f)
        update(delta)
        render(camera.viewProj())
    }

    private fun update(delta: Duration) {
        camera.update(delta)
        val x =  sin(elapsed.elapsedNow().inSeconds).toFloat()
        val z =  cos(elapsed.elapsedNow().inSeconds).toFloat()
        spotLight.pos.set(camera.pos)
        spotLight.dir.set(camera.dir)
        shader.updateLightDir(spotLight)
        shader.updateLightPos(spotLight)

        // light.color.set(x,1f,z)
        //shader.updateLightColor(light)
        //lightModel.material.emissColor.set(light.color)
    }

    private fun render(viewProj: Matrix4fc) {
        shader.setUniform("projView", viewProj)
        shader.setUniform("viewPos", camera.pos)
        vao.bind()
        models.forEach {
            it.bind()
            vao.initAttributes()
            shader.setUniform("model", it.matrix)
            shader.setUniform("worldNormals", it.worldNormalsC)
            it.material.bind(shader)
            glDrawElements(GL_TRIANGLES, it.data.indices)
        }
    }

    private fun genGround() {
        val nb = 101f
        val ground = Cube(
            material = Material.smoothGray
        )

        ground.scale(nb, 1f, nb)
        ground.translate(y = -0.5f)
        models.add(ground)
    }

    private fun genBoxes(mapSize: Float) {
        val size = rnd.nextFloat() * 10 + 1
        val x = rnd.nextFloat().mapOne(-mapSize, mapSize)
        val z = rnd.nextFloat().mapOne(-mapSize, mapSize)
        val cube = Cube(material = Material.woodCrate)
        /// cube.rotate(PI/2)

        cube.setTranslate(x.rounded(), size / 2, z.rounded())
        models.add(cube)
    }


    override fun dispose() {}

    override fun mouseButtonPressed(buttons: Set<MouseButton>, mods: Modifiers) {
        if (MouseButton.Left in buttons) window.captureCursor()
    }

    override fun keyDown(keys: Set<Key>, mods: Modifiers) {
        if (Key.W in keys) camera.forward()
        if (Key.S in keys) camera.backward()
        if (Key.A in keys) camera.left()
        if (Key.D in keys) camera.right()
    }

    override fun keyPressed(keys: Set<Key>, mods: Modifiers) {
        if (Key.C in keys) camera.zoom(2f)
        if (Key.Escape in keys) window.freeCursor()
    }

    override fun keyReleased(keys: Set<Key>, mods: Modifiers) {
        if (Key.C in keys) camera.unZoom()
    }

    override fun mouseMoved(dx: Int, dy: Int, posX: Int, posY: Int) {
        camera.rotate(dy.toFloat(), dx.toFloat(), 0f)
    }

    override fun resized(width: Int, height: Int) {
        val aspect = width.toFloat() / height
        camera.setAspect(aspect)
    }
}

fun main() {
    val app = AppIml()
    app.launch()
}
