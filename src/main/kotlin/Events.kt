import org.lwjgl.glfw.GLFW.*

const val DELTA_MAX = 200

object EventHandler {

    private val modifiers = Modifiers()
    private val CTRL
        get() = modifiers.ctrl
    private val ALT
        get() = modifiers.alt
    private val SHIFT
        get() = modifiers.shift
    private val WINDOWS
        get() = modifiers.windows
    private val CAPS
        get() = modifiers.capsLock
    private val NUM
        get() = modifiers.numLock

    private val keysJustPressed = mutableSetOf<Key>()
    private val keysDown = mutableSetOf<Key>()
    private val keysJustReleased = mutableSetOf<Key>()

    private val buttonsJustPressed = mutableSetOf<MouseButton>()
    private val buttonsDown = mutableSetOf<MouseButton>()
    private val buttonsJustReleased = mutableSetOf<MouseButton>()

    private val eventListeners = mutableListOf<EventListener>()

    private var lastMouseX = -1
    private var lastMouseY = -1

    fun addListener(listener: EventListener) {
        eventListeners.add(listener)
    }

    fun update() {
        if (keysDown.isNotEmpty()) {
            eventListeners.forEach { it.keyDown(keysDown, modifiers) }
        }
        if (buttonsDown.isNotEmpty()) {
            eventListeners.forEach { it.mouseButtonDown(buttonsDown, modifiers) }
        }
    }

    fun keyStateChanged(key: Key, action: ActionState, mods: Int) {
        modifiers.set(mods)
        when (action) {
            ActionState.Pressed -> {
                keysDown.add(key)
                keysJustPressed.add(key)
                eventListeners.forEach { it.keyPressed(keysJustPressed, modifiers) }
                keysJustPressed.remove(key)
            }
            ActionState.Released -> {
                keysDown.remove(key)
                keysJustReleased.add(key)
                eventListeners.forEach { it.keyReleased(keysJustReleased, modifiers) }
                keysJustReleased.remove(key)
            }
            else -> Unit
        }
    }

    fun resized(width: Int, height: Int) {
        eventListeners.forEach { it.resized(width, height) }
    }

    fun moved(posX: Int, posY: Int) {
        eventListeners.forEach { it.moved(posX, posY) }
    }

    fun closed() {
        eventListeners.forEach { it.closed() }
    }

    fun focused(focusState: Boolean) {
        if (focusState) eventListeners.forEach { it.focused() }
        else eventListeners.forEach { it.unfocused() }
    }

    fun minimized(minimiseState: Boolean) {
        if (minimiseState) eventListeners.forEach { it.minimized() }
        else eventListeners.forEach { it.restored() }
    }

    fun maximized(maximiseState: Boolean) {
        if (maximiseState) eventListeners.forEach { it.maximized() }
        else eventListeners.forEach { it.restored() }
    }

    fun mouseEntered(entered: Boolean) {
        if (entered) eventListeners.forEach { it.entered() }
        else eventListeners.forEach { it.exited() }
    }

    fun mouseMoved(mouseX: Double, mouseY: Double) {
        val mouseXi = mouseX.toInt()
        val mouseYi = mouseY.toInt()
        var dx = mouseXi - lastMouseX
        var dy = mouseYi - lastMouseY

        if (dx > DELTA_MAX || dy > DELTA_MAX) {
            dx = 0
            dy = 0
        }
        eventListeners.forEach { it.mouseMoved(dx, dy, mouseXi, mouseYi) }
        lastMouseX = mouseXi
        lastMouseY = mouseYi
    }

    fun mouseClicked(button: MouseButton, action: ActionState, mods: Int) {
        modifiers.set(mods)
        if (action == ActionState.Pressed) {
            buttonsDown.add(button)
            buttonsJustPressed.add(button)
            eventListeners.forEach { it.mouseButtonPressed(buttonsJustPressed, modifiers) }
            buttonsJustPressed.clear()
        } else if (action == ActionState.Released) {
            buttonsDown.remove(button)
            buttonsJustReleased.add(button)
            eventListeners.forEach { it.mouseButtonReleased(buttonsJustPressed, modifiers) }
            buttonsJustReleased.remove(button)
        }
    }

    fun mouseScrolled(xOffset: Double, yOffset: Double) {
        eventListeners.forEach { it.mouseScrolled(yOffset.toInt()) }
    }

    fun isKeyDown(key: Key) = keysDown.contains(key)

    fun isMouseButtonDown(button: MouseButton) = button in buttonsDown

}

interface EventListener {
    fun resized(width: Int, height: Int) = Unit
    fun moved(posX: Int, posY: Int) = Unit
    fun maximized() = Unit
    fun closed() = Unit
    fun minimized() = Unit
    fun restored() = Unit
    fun focused() = Unit
    fun unfocused() = Unit
    fun entered() = Unit
    fun exited() = Unit
    fun keyPressed(keys: Set<Key>, mods: Modifiers) = Unit
    fun keyDown(keys: Set<Key>, mods: Modifiers) = Unit
    fun keyReleased(keys: Set<Key>, mods: Modifiers) = Unit
    fun mouseMoved(dx: Int, dy: Int, posX: Int, posY: Int) = Unit
    fun mouseScrolled(pixelOffset: Int) = Unit
    fun mouseButtonPressed(buttons: Set<MouseButton>, mods: Modifiers) = Unit
    fun mouseButtonReleased(buttons: Set<MouseButton>, mods: Modifiers) = Unit
    fun mouseButtonDown(buttons: Set<MouseButton>, mods: Modifiers) = Unit
}

@Suppress("MemberVisibilityCanBePrivate")
class Modifiers {
    var ctrl: Boolean = false
        private set
    var alt: Boolean = false
        private set
    var shift: Boolean = false
        private set
    var windows: Boolean = false
        private set
    var numLock: Boolean = false
        private set
    var capsLock: Boolean = false
        private set

    internal fun set(mods: Int) {
        ctrl = (mods and CTRL) != 0
        alt = (mods and ALT) != 0
        shift = (mods and SHIFT) != 0
        windows = (mods and WINDOWS) != 0
        numLock = (mods and NUM_LOCK) != 0
        capsLock = (mods and CAPSLOCK) != 0
    }

    companion object {
        const val CTRL = 1
        const val ALT = 1.shl(1)
        const val SHIFT = 1.shl(2)
        const val WINDOWS = 1.shl(3)
        const val NUM_LOCK = 1.shl(4)
        const val CAPSLOCK = 1.shl(5)
    }
}

enum class Key {
    Unknown,
    Escape,
    F1, F2, F3, F4, F5, F6, F7, F8, F9, F10, F11, F12,
    PrintScreen, ScrollLock, Pause,
    Insert, Home, PageUp, PageDown, End, Delete,
    Up, Down, Left, Right,
    NumLock, NpDivide, NpMultiply, NpMinus, NpPlus, NpEnter, NpDot,
    Grave,
    Np0, Np1, Np2, Np3, Np4, Np5, Np6, Np7, Np8, Np9,
    Kb0, Kb1, Kb2, Kb3, Kb4, Kb5, Kb6, Kb7, Kb8, Kb9, Minus, Equals,
    RightBracket, LeftBracket,
    A, B, C, D, E, F, G, H, I, J, K, L, M, N, O, P, Q, R, S, T, U, V, W, X, Y, Z,
    Comma, Dot, Semicolon, Slash, Backslash, Apostrophe,
    Tab, CapsLock, LeftShift, LeftControl,
    Windows, LeftAlt, Space, RightAlt, RightControl,
    RightShift, Enter, Backspace,
}

enum class MouseButton {
    Left, Right, Middle, Unknown
}

enum class ActionState {
    Pressed, Released, Repeated, Unknown
}

interface EventMapper {

    fun convertKey(libraryKey: Int): Key

    fun convertMouse(libraryMouseButton: Int): MouseButton

    fun convertKeyStateAction(libraryKeyAction: Int): ActionState

    fun convertMods(libraryMods: Int): Int
}

class EventMapperGl : EventMapper {

    override fun convertKey(libraryKey: Int) = when (libraryKey) {
        GLFW_KEY_A -> Key.A
        GLFW_KEY_B -> Key.B
        GLFW_KEY_C -> Key.C
        GLFW_KEY_D -> Key.D
        GLFW_KEY_E -> Key.E
        GLFW_KEY_F -> Key.F
        GLFW_KEY_G -> Key.G
        GLFW_KEY_H -> Key.H
        GLFW_KEY_I -> Key.I
        GLFW_KEY_J -> Key.J
        GLFW_KEY_K -> Key.K
        GLFW_KEY_L -> Key.L
        GLFW_KEY_M -> Key.M
        GLFW_KEY_N -> Key.N
        GLFW_KEY_O -> Key.O
        GLFW_KEY_P -> Key.P
        GLFW_KEY_Q -> Key.Q
        GLFW_KEY_R -> Key.R
        GLFW_KEY_S -> Key.S
        GLFW_KEY_T -> Key.T
        GLFW_KEY_U -> Key.U
        GLFW_KEY_V -> Key.V
        GLFW_KEY_W -> Key.W
        GLFW_KEY_X -> Key.X
        GLFW_KEY_Y -> Key.Y
        GLFW_KEY_Z -> Key.Z
        GLFW_KEY_1 -> Key.Kb1
        GLFW_KEY_2 -> Key.Kb2
        GLFW_KEY_3 -> Key.Kb3
        GLFW_KEY_4 -> Key.Kb4
        GLFW_KEY_5 -> Key.Kb5
        GLFW_KEY_6 -> Key.Kb6
        GLFW_KEY_7 -> Key.Kb7
        GLFW_KEY_8 -> Key.Kb8
        GLFW_KEY_9 -> Key.Kb9
        GLFW_KEY_0 -> Key.Kb0
        GLFW_KEY_ENTER -> Key.Enter
        GLFW_KEY_ESCAPE -> Key.Escape
        GLFW_KEY_BACKSPACE -> Key.Backspace
        GLFW_KEY_TAB -> Key.Tab
        GLFW_KEY_SPACE -> Key.Space
        GLFW_KEY_MINUS -> Key.Minus
        GLFW_KEY_EQUAL -> Key.Equals
        GLFW_KEY_LEFT_BRACKET -> Key.LeftBracket
        GLFW_KEY_RIGHT_BRACKET -> Key.RightBracket
        GLFW_KEY_BACKSLASH -> Key.Backslash
        GLFW_KEY_SEMICOLON -> Key.Semicolon
        GLFW_KEY_APOSTROPHE -> Key.Apostrophe
        GLFW_KEY_GRAVE_ACCENT -> Key.Grave
        GLFW_KEY_COMMA -> Key.Comma
        GLFW_KEY_PERIOD -> Key.Dot
        GLFW_KEY_SLASH -> Key.Slash
        GLFW_KEY_CAPS_LOCK -> Key.CapsLock
        GLFW_KEY_F1 -> Key.F1
        GLFW_KEY_F2 -> Key.F2
        GLFW_KEY_F3 -> Key.F3
        GLFW_KEY_F4 -> Key.F4
        GLFW_KEY_F5 -> Key.F5
        GLFW_KEY_F6 -> Key.F6
        GLFW_KEY_F7 -> Key.F7
        GLFW_KEY_F8 -> Key.F8
        GLFW_KEY_F9 -> Key.F9
        GLFW_KEY_F10 -> Key.F10
        GLFW_KEY_F11 -> Key.F11
        GLFW_KEY_F12 -> Key.F12
        GLFW_KEY_PRINT_SCREEN -> Key.PrintScreen
        GLFW_KEY_SCROLL_LOCK -> Key.ScrollLock
        GLFW_KEY_PAUSE -> Key.Pause
        GLFW_KEY_INSERT -> Key.Insert
        GLFW_KEY_HOME -> Key.Home
        GLFW_KEY_PAGE_UP -> Key.PageUp
        GLFW_KEY_DELETE -> Key.Delete
        GLFW_KEY_END -> Key.End
        GLFW_KEY_PAGE_DOWN -> Key.PageDown
        GLFW_KEY_RIGHT -> Key.Right
        GLFW_KEY_LEFT -> Key.Left
        GLFW_KEY_DOWN -> Key.Down
        GLFW_KEY_UP -> Key.Up
        GLFW_KEY_NUM_LOCK -> Key.NumLock
        GLFW_KEY_KP_DIVIDE -> Key.NpDivide
        GLFW_KEY_KP_MULTIPLY -> Key.NpMultiply
        GLFW_KEY_KP_SUBTRACT -> Key.NpMinus
        GLFW_KEY_KP_ADD -> Key.NpPlus
        GLFW_KEY_KP_ENTER -> Key.NpEnter
        GLFW_KEY_KP_1 -> Key.Np1
        GLFW_KEY_KP_2 -> Key.Np2
        GLFW_KEY_KP_3 -> Key.Np3
        GLFW_KEY_KP_4 -> Key.Np4
        GLFW_KEY_KP_5 -> Key.Np5
        GLFW_KEY_KP_6 -> Key.Np6
        GLFW_KEY_KP_7 -> Key.Np7
        GLFW_KEY_KP_8 -> Key.Np8
        GLFW_KEY_KP_9 -> Key.Np9
        GLFW_KEY_KP_0 -> Key.Np0
        GLFW_KEY_KP_DECIMAL -> Key.NpDot
        GLFW_KEY_LEFT_SUPER, GLFW_KEY_RIGHT_SUPER -> Key.Windows
        GLFW_KEY_LEFT_SHIFT -> Key.LeftShift
        GLFW_KEY_LEFT_CONTROL -> Key.LeftControl
        GLFW_KEY_LEFT_ALT -> Key.LeftAlt
        GLFW_KEY_RIGHT_ALT -> Key.RightAlt
        GLFW_KEY_RIGHT_CONTROL -> Key.RightControl
        GLFW_KEY_RIGHT_SHIFT -> Key.RightShift
        else -> Key.Unknown
    }

    override fun convertMouse(libraryMouseButton: Int) = when (libraryMouseButton) {
        GLFW_MOUSE_BUTTON_RIGHT -> MouseButton.Right
        GLFW_MOUSE_BUTTON_MIDDLE -> MouseButton.Middle
        GLFW_MOUSE_BUTTON_LEFT -> MouseButton.Left
        else -> MouseButton.Unknown
    }

    override fun convertKeyStateAction(libraryKeyAction: Int) = when (libraryKeyAction) {
        GLFW_PRESS -> ActionState.Pressed
        GLFW_RELEASE -> ActionState.Released
        GLFW_REPEAT -> ActionState.Repeated
        else -> ActionState.Unknown
    }

    override fun convertMods(libraryMods: Int): Int {
        var mods = 0
        if (GLFW_MOD_CONTROL and libraryMods == 1) mods += Modifiers.CTRL
        if (GLFW_MOD_ALT and libraryMods == 1) mods += Modifiers.ALT
        if (GLFW_MOD_SHIFT and libraryMods == 1) mods += Modifiers.SHIFT
        if (GLFW_MOD_CAPS_LOCK and libraryMods == 1) mods += Modifiers.CAPSLOCK
        if (GLFW_MOD_SUPER and libraryMods == 1) mods += Modifiers.WINDOWS
        if (GLFW_MOD_NUM_LOCK and libraryMods == 1) mods += Modifiers.NUM_LOCK
        return mods
    }
}
