import org.lwjgl.opengl.GL20.*
import org.lwjgl.opengl.GL30.GL_FRAMEBUFFER_SRGB
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.TimeMark
import kotlin.time.TimeSource

val elapsed = TimeSource.Monotonic.markNow()

abstract class App() : EventListener(){
    private var shouldRun = true
    protected var window = Window
    val now : Duration
        get() = TimeSource.Monotonic.markNow().elapsedNow()
    val seconds : Double
        get() =  now.inSeconds
    val millisec : Double
        get() = now.inMilliseconds
    var wireFrame = false
        private set

    init {
        window.initCallbacks(EventHandler)
    }

    abstract fun init()

    abstract fun tick(delta : Duration)

    abstract fun dispose()

    fun launch() {
        init()
        //glPolygonMode( GL_FRONT_AND_BACK, GL_LINE )
        glEnable(GL_FRAMEBUFFER_SRGB);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        glFrontFace(GL_CCW);

        var last = TimeSource.Monotonic.markNow()
        var current = TimeSource.Monotonic.markNow()

        while (shouldRun && !window.shouldClose()){
            val delta = current.minus(last.elapsedNow())
            glEnable(GL_DEPTH_TEST)
            glClear(GL_COLOR_BUFFER_BIT or GL_DEPTH_BUFFER_BIT)
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

    fun toggleWireFrame(){
        if( wireFrame) {
            wireFrame = false
            glPolygonMode(GL_FRONT, GL_FILL);
            glPolygonMode(GL_BACK, GL_FILL)
        } else {
            wireFrame = true
            glPolygonMode(GL_FRONT, GL_LINE);
            glPolygonMode(GL_BACK, GL_LINE);
        }
    }


    fun stop(){
        shouldRun = false
    }
}
