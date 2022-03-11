import org.joml.Matrix4fc
import org.joml.Vector3f
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.opengl.GL11.glClearColor
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30
import javax.swing.text.html.HTMLDocument
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random
import kotlin.time.Duration

class AppGl : App(WindowGl()) {

    private val capabilities = GL.createCapabilities()
    private val modelRenderer = ModelRendererGl()
    private val shader = BlinnPhongShaderGl()
    private val camera = FpsCamera(90f, 10000f, 16.0f / 9)
    private val lightPreset = LightPreset(ambient = 0.2f, diffuse = 0.5f)

    private val light = PointLight(
        lightPreset, shader = shader, range = 500f, pos = Vector3f(0f, 10f, 0f),
    )

    private val spotLight = SpotLight(
        lightPreset, shader = shader, cutAngleDef = 15f.rad(), range = 200f
    )

    private val lightModel = Cube(material = Material.whiteCube)

    private val rnd = Random(3453)

    override fun init() {
        glEnable(GL30.GL_FRAMEBUFFER_SRGB);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        shader.use()
        shader.addLight(light)
        shader.addLight(spotLight)
        camera.setPos(-10f, 2f, 0f)

        genGround()
        for (i in 0..3) genBoxes(20f)
        modelRenderer.addModel(lightModel)
        lightModel.scale(0.3f, 0.3f, 0.3f)
        lightModel.translate(light.pos)
        val sphere = Sphere(Material.smoothGray)
        sphere.scale(2f)
        sphere.translate(10f, 10f, 10f)

        val window = Model(objToModelData("window.obj"), Material.window)
        window.rotate(x = PI / 2)
        window.translate(0f, 2f, 0f)
        modelRenderer.addModel(window)
        modelRenderer.addModel(sphere)
        //frameBuffer.bind()

    }

    private fun reloadShader() {
        shader.reload()
        shader.addLight(light)
        shader.addLight(spotLight)
    }

    override fun tick(delta: Duration) {
        glEnable(GL_DEPTH_TEST)
        glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
        glClearColor(0f, 0.3f, 0.3f, 1f)
        update(delta)
        render(camera.getViewProj())
    }

    private fun update(delta: Duration) {
        camera.update(delta)
        val x = sin(elapsed.elapsedNow().inSeconds).toFloat()
        val z = cos(elapsed.elapsedNow().inSeconds).toFloat()
        spotLight.setPos(camera.pos)
        spotLight.setDir(camera.dir)
    }

    private fun render(viewProj: Matrix4fc) {
        checkWireFrame()
        shader.setUniform("projView", viewProj)
        shader.setUniform("viewPos", camera.pos)
        modelRenderer.renderModels(shader)
    }

    private fun checkWireFrame() {
        if (wireFrame) {
            glPolygonMode(GL_FRONT, GL_LINE);
            glPolygonMode(GL_BACK, GL_LINE);
        } else {
            glPolygonMode(GL_FRONT, GL_FILL);
            glPolygonMode(GL_BACK, GL_FILL)
        }
    }

    private fun genGround() {
        val ground = Model(objToModelData("wood_floor.obj"), Material.woodFloor)
        modelRenderer.addModel(ground)
    }

    private fun genBoxes(mapSize: Float) {
        val size = rnd.nextFloat() * 10 + 3
        val x = rnd.nextFloat().mapOne(-mapSize, mapSize)
        val z = rnd.nextFloat().mapOne(-mapSize, mapSize)
        val cube = Cube(material = Material.concrete)
        cube.scale(3f)
        cube.translate(x.rounded(), size / 2, z.rounded())
        modelRenderer.addModel(cube)
    }

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
        if (Key.Z in keys) toggleWireFrame()
        if (Key.R in keys) reloadShader()
        if (Key.B in keys) spotLight.intensity = 0f
        if (Key.N in keys) spotLight.intensity = 1f
    }

    override fun keyReleased(keys: Set<Key>, mods: Modifiers) {
        if (Key.C in keys) camera.unZoom()
    }

    override fun mouseMoved(dx: Int, dy: Int, posX: Int, posY: Int) {
        if (!EventHandler.isKeyDown(Key.Space))
            camera.rotate(dy.toFloat(), dx.toFloat(), 0f)
    }

    override fun resized(width: Int, height: Int) {
       GL11.glViewport(0, 0, width, height)
        camera.aspect = width.toFloat() / height
    }

    override fun dispose() {}
}

fun main() {

    val app = AppGl()
    app.launch()
}
