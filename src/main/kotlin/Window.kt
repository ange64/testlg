import org.lwjgl.glfw.Callbacks.glfwFreeCallbacks
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import org.lwjgl.opengl.GL11
import org.lwjgl.system.MemoryUtil.NULL

const val DEF_HEIGHT = 1000
const val DEF_WIDTH = 1600

interface Window {
    val handle: Long
    val width: Int
    val height: Int
    val x: Int
    val y: Int
    val aspect: Float
    val eventMapper: EventMapper

    fun setCallbacks(evh: EventHandler)

    fun shouldClose(): Boolean

    fun close()

    fun pollEvents()

    fun swapBuffers()

    fun vSync(state: Boolean)

    fun resize(width: Int, height: Int)

    fun setPos(x: Int, y: Int)

    fun captureCursor()

    fun freeCursor()

    fun getCursorCaptureState(): Boolean

    fun destroy()

}

class WindowGl : Window {

    override val handle: Long
    override val width: Int
        get() = getW()
    override val height: Int
        get() = getH()
    override val x: Int
        get() = getXpos()
    override val y: Int
        get() = getYpos()
    override val aspect: Float
        get() = width /( height * 1f)
    override val eventMapper = EventMapperGl()

    init {
        if (!glfwInit()) throw IllegalStateException("unable to init")
        glfwWindowHint(GLFW_VISIBLE, TRUE)
        glfwWindowHint(GLFW_RESIZABLE, TRUE)
        handle = glfwCreateWindow(DEF_WIDTH, DEF_HEIGHT, "window", NULL, NULL)
        if (handle.isNull()) throw RuntimeException("Failed to create the GLFW window");
        glfwMakeContextCurrent(handle)
        glfwSwapInterval(1)
        glfwShowWindow(handle)
    }

    private fun getW(): Int {
        val buffer = intArrayOf(0)
        glfwGetWindowSize(handle, buffer, buffer)
        return buffer[0]
    }

    private fun getH(): Int {
        val buffer = intArrayOf(0)
        glfwGetWindowSize(handle, null, buffer)
        return buffer[0]
    }

    private fun getXpos(): Int {
        val buffer = intArrayOf(0)
        glfwGetWindowPos(handle, buffer, null)
        return buffer[0]
    }

    private fun getYpos(): Int {
        val buffer = intArrayOf(0)
        glfwGetWindowSize(handle, null, buffer)
        return buffer[0]
    }


    override fun setCallbacks(evh: EventHandler) {
        glfwSetKeyCallback(handle) { _, key, _, action, mods ->
            evh.keyStateChanged(
                eventMapper.convertKey(key),
                eventMapper.convertKeyStateAction(action),
                eventMapper.convertMods(mods)
            )
        }
        glfwSetMouseButtonCallback(handle) { _, button, action, mods ->
            evh.mouseClicked(
                eventMapper.convertMouse(button),
                eventMapper.convertKeyStateAction(action),
                eventMapper.convertMods(mods)
            ) }
        glfwSetWindowSizeCallback(handle) { _, w, h ->
            evh.resized(w, h)
        }
        glfwSetWindowCloseCallback(handle) { evh.closed() }
        glfwSetWindowFocusCallback(handle) { _, state -> evh.focused(state) }
        glfwSetWindowIconifyCallback(handle) { _, state -> evh.minimized(state) }
        glfwSetWindowMaximizeCallback(handle) { _, state -> evh.maximized(state) }
        glfwSetWindowPosCallback(handle) { _, x, y -> evh.moved(x, y) }
        glfwSetCursorPosCallback(handle) { _, x, y -> evh.mouseMoved(x, y) }

        glfwSetScrollCallback(handle) { _, xOff, yOff -> evh.mouseScrolled(xOff, yOff) }
        glfwSetCursorEnterCallback(handle) { _, state -> evh.mouseEntered(state) }
    }

    override fun shouldClose() = glfwWindowShouldClose(handle)

    override fun close() {
        glfwSetWindowShouldClose(handle, true)
    }

    override fun pollEvents() {
        glfwPollEvents()
    }

    override fun swapBuffers() {
        glfwSwapBuffers(handle)
    }

    override fun vSync(state: Boolean) {
        glfwSwapInterval(if (state) 1 else 0)
    }

    override fun resize(width: Int, height: Int) {
        glfwSetWindowSize(handle, width, height)
    }

    override fun setPos(x: Int, y: Int) {
        glfwSetWindowPos(handle, x, y)
    }

    override fun captureCursor() {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
    }

    override fun freeCursor() {
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
    }

    override fun getCursorCaptureState(): Boolean {
        return glfwGetInputMode(handle, GLFW_CURSOR) == GLFW_CURSOR_DISABLED
    }

    override fun destroy() {
        glfwDestroyWindow(handle)
        glfwFreeCallbacks(handle)
        glfwTerminate()
    }

}


