import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB
import kotlin.time.Duration
import kotlin.time.TimeSource

val elapsed = TimeSource.Monotonic.markNow()

abstract class App(
    protected val window: Window,
) : EventListener {

    private var shouldRun = true
    val now: Duration
        get() = TimeSource.Monotonic.markNow().elapsedNow()
    val seconds: Double
        get() = now.inSeconds
    val millisec: Double
        get() = now.inMilliseconds
    var wireFrame = false
        private set

    init {
        window.setCallbacks(EventHandler)
        EventHandler.addListener(this)
    }

    abstract fun init()

    abstract fun tick(delta: Duration)

    abstract fun dispose()

    fun launch() {
        init()

        var last = TimeSource.Monotonic.markNow()
        var current = TimeSource.Monotonic.markNow()

        while (shouldRun && !window.shouldClose()) {
            val delta = current.minus(last.elapsedNow())
            tick(delta.elapsedNow())
            window.swapBuffers()
            window.pollEvents()
            EventHandler.update()
            last = current
            current = TimeSource.Monotonic.markNow()
        }

        window.destroy()
        dispose()
    }

    fun toggleWireFrame() {
        wireFrame = !wireFrame
    }

    fun stop() {
        shouldRun = false
    }
}
