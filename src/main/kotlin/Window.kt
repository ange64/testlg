import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFWVidMode
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11.glViewport
import org.lwjgl.system.MemoryUtil.NULL
import java.lang.IllegalStateException
import kotlin.properties.ObservableProperty

const val DEF_HEIGHT = 1000
const val DEF_WIDTH = 1600

 object Window  {
    private val handle: Long

    var width : Int = DEF_HEIGHT
        private set
    var height : Int = DEF_WIDTH
        private set

    var x : Int = 0
        private set

    var y : Int = 0
        private set

    val aspect : Float = width/ height * 1f

    val screenSize: GLFWVidMode
        get() = glfwGetVideoMode(glfwGetPrimaryMonitor())!!

    init {
        GLFWErrorCallback.createPrint(System.err).set()
        if (!glfwInit()) throw IllegalStateException("unable to init")
        glfwWindowHint(GLFW_VISIBLE, TRUE)
        glfwWindowHint(GLFW_RESIZABLE, TRUE)
        handle = glfwCreateWindow(DEF_WIDTH, DEF_HEIGHT, "window", NULL, NULL)
        if (handle.isNull()) throw RuntimeException("Failed to create the GLFW window");
        glfwMakeContextCurrent(handle)
        glfwSwapInterval(1)
        glfwShowWindow(handle)
        GL.createCapabilities()
    }

    fun initCallbacks(evh : EventHandler){
        glfwSetKeyCallback(handle, evh::keyStateChanged)
        glfwSetWindowSizeCallback(handle,evh::resized)
        glfwSetWindowCloseCallback(handle,evh::closed)
        glfwSetWindowFocusCallback(handle,evh::focused)
        glfwSetWindowIconifyCallback(handle,evh::minimized)
        glfwSetWindowMaximizeCallback(handle,evh::maximized)
        glfwSetWindowPosCallback(handle,evh::moved)
        glfwSetCursorPosCallback(handle,evh::mouseMoved)
        glfwSetMouseButtonCallback(handle,evh::mouseClicked)
        glfwSetScrollCallback(handle,evh::mouseScrolled)
        glfwSetCursorEnterCallback(handle,evh::mouseEntered)
    }

    fun shouldClose() = glfwWindowShouldClose(handle)

    fun close() {
        glfwSetWindowShouldClose(handle, true)
    }

    fun pollEvents() {
        glfwPollEvents()
    }

    fun swapBuffers() {
        glfwSwapBuffers(handle)
    }

    fun vsynch(state : Boolean){
        glfwSwapInterval(if(state) 1 else 0)
    }

    fun captureCursor()  {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    fun freeCursor(){
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    fun cursorCaptured() = glfwGetInputMode(handle,GLFW_CURSOR) == GLFW_CURSOR_DISABLED

    fun destroy() {
        glfwDestroyWindow(handle)
        glfwFreeCallbacks(handle)
        glfwTerminate()
        glfwSetErrorCallback(null)!!.free();
    }
}