package lime.editor

import lime.Lime
import lime.input.Keys
import lime.utils.Log
import lwjgui.LWJGUI
import lwjgui.geometry.Insets
import lwjgui.geometry.Pos
import lwjgui.scene.Scene
import lwjgui.scene.Window
import lwjgui.scene.control.*
import lwjgui.scene.layout.*
import lwjgui.theme.Theme
import lwjgui.theme.ThemeDark

object Editor {
    var window: Window? = null
        private set

    private lateinit var editScene: Scene
    private lateinit var playScene: Scene

    init {
        Lime.inEditorMode {
            Theme.setTheme(ThemeDark())

            window = LWJGUI.initialize(Lime.window.handle)
            window ?: Log.fail(this::class, "Failed to initialize LWJGUI")
            requireNotNull(window).setWindowAutoDraw(false)
            editScene = createEditScene()
            playScene = createPlayScene()

            requireNotNull(window).scene = editScene
            requireNotNull(window).show()

            Lime.addPreFrameListener {
                if (window?.scene == playScene && Lime.input.isKeyTyped(Keys.KEY_ESCAPE))
                    requireNotNull(window).scene = editScene
            }
        }
    }

    private fun createEditScene(): Scene {
        val root = BorderPane()

        root.setTop(MenuBar().apply {
            items.add(Menu("Project").apply {
                items.add(MenuItem("Run").apply {
                    setOnAction {
                        requireNotNull(window).scene = playScene
                    }
                })
                items.add(MenuItem("Close").apply {
                    setOnAction {
                        Lime.stop()
                    }
                })
            })
        })

        root.setCenter(OpenGLPane().apply {
            background = null
            isFillToParentWidth = true
            isFillToParentHeight = true

            setRendererCallback {
                Lime.graphics.beginFrame()
                Lime.application.onFrame(0.0)
                Lime.scenes.processAll(0.0f)
                Lime.views.render()
                Lime.graphics.endFrame()
            }
        })

        root.setLeft(BorderPane().apply {
            setTop(Label("Scene"))
            setCenter(ScrollPane().apply {
                isFillToParentHeight = true

                content = TreeView<String>().apply {
                    items.add(TreeItem("A"))
                    items.add(TreeItem("B"))
                    items.add(TreeItem("C"))
                    items.add(TreeItem("D"))
                    items.add(TreeItem("E"))
                }
            })
        })

        return Scene(root, Lime.window.width.toDouble(), Lime.window.height.toDouble())
    }

    private fun createPlayScene(): Scene {
        val root = BorderPane()
        root.background = null

        root.setCenter(OpenGLPane().apply {
            setPrefSize(Lime.window.width.toDouble(), Lime.window.height.toDouble())
            setRendererCallback {
                Lime.graphics.beginFrame()
                Lime.application.onFrame(Lime.deltaTime)
                Lime.views.render()
                Lime.graphics.endFrame()
            }
        })

        root.setTop(BlurPane().apply {
            padding = Insets(8.0)
            children.add(Button("Back to Editor").apply {
                setOnAction {
                    requireNotNull(window).scene = editScene
                }
            })
        })

        return Scene(root, Lime.window.width.toDouble(), Lime.window.height.toDouble())
    }
}