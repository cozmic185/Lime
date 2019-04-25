package lime.input

import lime.Lime
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

typealias KeyDownListener = (Key) -> Unit
typealias KeyUpListener = (Key) -> Unit
typealias ButtonDownListener = (Button) -> Unit
typealias ButtonUpListener = (Button) -> Unit
typealias MouseMovedListener = (Int, Int) -> Unit
typealias ScrolledListener = (Int) -> Unit

class Input {
    private val keyDownListeners = arrayListOf<KeyDownListener>()
    private val keyUpListeners = arrayListOf<KeyUpListener>()
    private val buttonDownListeners = arrayListOf<ButtonDownListener>()
    private val buttonUpListeners = arrayListOf<ButtonUpListener>()
    private val mouseMovedListeners = arrayListOf<MouseMovedListener>()
    private val scrolledListeners = arrayListOf<ScrolledListener>()

    private var keys = BooleanArray(Keys.values().size) { false }
    private var keyStates = BooleanArray(Keys.values().size) { false }
    private var keysTyped = BooleanArray(Keys.values().size) { false }

    private var buttons = BooleanArray(Buttons.values().size) { false }
    private var buttonStates = BooleanArray(Buttons.values().size) { false }
    private var buttonsClicked = BooleanArray(Buttons.values().size) { false }

    init {
        addKeyDownListener {
            keys[it.ordinal] = true
        }

        addKeyUpListener {
            keys[it.ordinal] = false
        }

        addButtonDownListener {
            buttons[it.ordinal] = true
        }

        addButtonUpListener {
            buttons[it.ordinal] = false
        }
    }

    fun addKeyDownListener(listener: KeyDownListener) = synchronized(keyDownListeners) {
        keyDownListeners.add(listener)
    }

    fun removeKeyDownListener(listener: KeyDownListener) = synchronized(keyDownListeners) {
        keyDownListeners.remove(listener)
    }

    fun addKeyUpListener(listener: KeyUpListener) = synchronized(keyUpListeners) {
        keyUpListeners.add(listener)
    }

    fun removeKeyUpListener(listener: KeyUpListener) = synchronized(keyUpListeners) {
        keyUpListeners.remove(listener)
    }

    fun addButtonDownListener(listener: ButtonDownListener) = synchronized(buttonDownListeners) {
        buttonDownListeners.add(listener)
    }

    fun removeButtonDownListener(listener: ButtonDownListener) = synchronized(buttonDownListeners) {
        buttonDownListeners.remove(listener)
    }

    fun addButtonUpListener(listener: ButtonUpListener) = synchronized(buttonUpListeners) {
        buttonUpListeners.add(listener)
    }

    fun removeButtonUpListener(listener: ButtonUpListener) = synchronized(buttonUpListeners) {
        buttonUpListeners.remove(listener)
    }

    fun addMouseMovedListener(listener: MouseMovedListener) = synchronized(mouseMovedListeners) {
        mouseMovedListeners.add(listener)
    }

    fun removeMouseMovedListener(listener: MouseMovedListener) = synchronized(mouseMovedListeners) {
        mouseMovedListeners.remove(listener)
    }

    fun addScrollListener(listener: ScrolledListener) = synchronized(scrolledListeners) {
        scrolledListeners.add(listener)
    }

    fun removeScrollListener(listener: ScrolledListener) = synchronized(scrolledListeners) {
        scrolledListeners.remove(listener)
    }

    internal fun onKeyDown(code: Int) {
        val key = Keys.of(code) ?: return
        keyDownListeners.forEach { it(key) }
    }

    internal fun onKeyUp(code: Int) {
        val key = Keys.of(code) ?: return
        keyUpListeners.forEach { it(key) }
    }

    internal fun onButtonDown(code: Int) {
        val button = Buttons.values()[code]
        buttonDownListeners.forEach { it(button) }
    }

    internal fun onButtonUp(code: Int) {
        val button = Buttons.values()[code]
        buttonUpListeners.forEach { it(button) }
    }

    internal fun onMouseMoved(x: Int, y: Int) {
        mouseMovedListeners.forEach { it(x, y) }
    }

    internal fun onScrolled(amount: Int) {
        scrolledListeners.forEach { it(amount) }
    }

    internal fun update() {
        repeat(keys.size) {
            keysTyped[it] = keys[it] && !keyStates[it]
            keyStates[it] = keys[it]
        }

        repeat(buttons.size) {
            buttonsClicked[it] = buttons[it] && !buttonStates[it]
            buttonStates[it] = buttons[it]
        }
    }

    fun isKeyDown(key: Key) = keys[key.ordinal]
    fun isButtonDown(button: Button) = buttons[button.ordinal]
    fun isKeyTyped(key: Key) = keysTyped[key.ordinal]
    fun isButtonClicked(button: Button) = buttonsClicked[button.ordinal]
}