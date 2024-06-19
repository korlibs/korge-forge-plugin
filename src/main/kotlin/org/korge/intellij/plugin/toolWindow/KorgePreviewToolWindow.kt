package org.korge.intellij.plugin.toolWindow

import androidx.compose.runtime.*
import com.intellij.icons.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.*
import com.intellij.ui.*
import com.intellij.ui.content.*
import com.intellij.util.ui.*
import com.soywiz.korge.intellij.util.*
import korge.composable.*
import korlibs.io.lang.*
import korlibs.korge.ipc.*
import korlibs.time.*
import java.awt.*
import java.awt.event.*
import java.awt.image.*
import javax.swing.*
import kotlin.jvm.optionals.*

@Composable
fun MyFunc() {
    var n by state(0)
    Button("hello $n") {
        n++
    }
    Button("world $n") {
        n = 20
    }
}

class KorgePreviewToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        //val panel = createComposableJPanel {
        //    MyFunc()
        //}

        val panel = KorgeIPCJPanel()

        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(panel, "", false))
        toolWindow.setTitleActions(listOf(
            object : KorgeAction("Play", "Play", AllIcons.Actions.Play_forward) {
                override fun actionPerformed(e: AnActionEvent) {
                }
            },
            object : KorgeAction("Stop", "Stop", AllIcons.Actions.StopRefresh) {
                override fun actionPerformed(e: AnActionEvent) {
                }
            }
        ))

        toolWindow.show()
    }
}

//class KorgeIPCJPanel(val ipc: KorgeIPC = KorgeIPC()) : JPanel(), MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener {
//class KorgeIPCJPanel(val ipc: KorgeIPC = KorgeIPC()) : JLabel(), MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener {
class KorgeIPCJPanel(val ipc: KorgeIPC = KorgeIPC()) : JButton(), MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener, ComponentListener {
    var lastFrame: IPCFrame? = null
    var image: BufferedImage? = null
    var currentFrameId = -1

    init {
        isFocusable = true
        addKeyListener(this)
        addMouseListener(this)
        addMouseMotionListener(this)
        addMouseWheelListener(this)
        addHierarchyListener(this)
        addFocusListener(this)
        addComponentListener(this)
        SwingUtilities.invokeLater { requestFocusInWindow() }
    }

    var checkProcessTimer = Stopwatch().start()
    fun doRenderLoop(): AutoCloseable {
        var running = true
        fun func() {
            if (!running) return
            if (checkProcessTimer.elapsed >= 0.25.seconds) {
                if (lastFrame != null) {
                    if (ProcessHandle.of(lastFrame!!.pid.toLong()).getOrNull()?.isAlive != true) {
                        ipc.setFrame(IPCFrame(-1, 0, 0, IntArray(0)))
                        image = null
                    }
                }

                checkProcessTimer.restart()
            }
            try {
                readFrame()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            SwingUtilities.invokeLater(::func)
        }
        func()
        return AutoCloseable { running = false }
    }

    var renderLoop: AutoCloseable? = null

    override fun hierarchyChanged(e: HierarchyEvent) {
        if ((e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong()) != 0L) {
            if (isShowing) {
                renderLoop?.close()
                renderLoop = doRenderLoop()
            } else {
                renderLoop?.close()
            }
        }
    }


    override fun paint(g: Graphics) {
        //println("Paint: ${Thread.currentThread()}")
        if (image != null) {
            g.drawImage(image, 0, 0, width, height, null)
        } else {
            g.color = JBColor.PanelBackground
            g.fillRect(0, 0, width, height)
            g.color = JBColor.foreground()
            g.drawString("Not running yet...", 25, 25)
        }
    }

    fun rgbaToBgra(v: Int): Int = ((v shl 16) and 0x00FF0000) or ((v shr 16) and 0x000000FF) or (v and 0xFF00FF00.toInt())

    fun readFrame() {
        val frameId = ipc.getFrameId()
        if (frameId == currentFrameId) {
            return
        } // Do not update
        val frame = ipc.getFrame()
        lastFrame = frame
        currentFrameId = frame.id
        if (frame.width == 0 || frame.height == 0) {
            this.image = null
            return
        } // Empty frame
        val image = BufferedImage(frame.width, frame.height, BufferedImage.TYPE_INT_ARGB)
        this.image = image
        val imgPixels = (image.raster.dataBuffer as DataBufferInt).data
        System.arraycopy(frame.pixels, 0, imgPixels, 0, frame.width * frame.height)
        for (n in imgPixels.indices) imgPixels[n] = rgbaToBgra(imgPixels[n])
        repaint()
    }

    val devicePixelRatio: Double get() {
        return JBUI.pixScale().toDouble()
        //val defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
        //val defaultConfiguration = defaultScreenDevice.defaultConfiguration
        //val defaultTransform = defaultConfiguration.defaultTransform
        //return defaultTransform.scaleX
    }

    private fun sendEv(type: Int, p0: Int = 0, p1: Int = 0, p2: Int = 0, p3: Int = 0) = ipc.writeEvent(IPCEvent(type = type, p0 = p0, p1 = p1, p2 = p2, p3 = p3))
    private fun sendEv(type: Int, e: KeyEvent) = sendEv(type = type, p0 = e.keyCode, p1 = e.keyChar.code)
    private fun sendEv(type: Int, e: MouseEvent) {
        //val ratio = 1.0 / devicePixelRatio
        val ratio = devicePixelRatio
        sendEv(type = type, p0 = (e.x * ratio).toInt(), p1 = (e.y * ratio).toInt(), p2 = e.button)
    }
    override fun keyTyped(e: KeyEvent) = sendEv(IPCEvent.KEY_TYPE, e)
    override fun keyPressed(e: KeyEvent) = sendEv(IPCEvent.KEY_DOWN, e)
    override fun keyReleased(e: KeyEvent) = sendEv(IPCEvent.KEY_UP, e)
    override fun mouseMoved(e: MouseEvent) = sendEv(IPCEvent.MOUSE_MOVE, e)
    override fun mouseDragged(e: MouseEvent) = sendEv(IPCEvent.MOUSE_MOVE, e)
    override fun mouseWheelMoved(e: MouseWheelEvent) = sendEv(IPCEvent.MOUSE_MOVE, e)
    override fun mouseExited(e: MouseEvent) = sendEv(IPCEvent.MOUSE_MOVE, e)
    override fun mouseEntered(e: MouseEvent) = sendEv(IPCEvent.MOUSE_MOVE, e)
    override fun mouseReleased(e: MouseEvent) = sendEv(IPCEvent.MOUSE_UP, e)
    override fun mousePressed(e: MouseEvent) {
        requestFocusInWindow()
        sendEv(IPCEvent.MOUSE_DOWN, e)
    }
    override fun mouseClicked(e: MouseEvent) {
        requestFocusInWindow()
        sendEv(IPCEvent.MOUSE_CLICK, e)
    }

    override fun componentResized(e: ComponentEvent) {
        println("componentResized: width=$width, height=$height")
        sendEv(IPCEvent.RESIZE, e.component.width, e.component.height)
    }
    override fun componentMoved(e: ComponentEvent) {
    }
    override fun componentShown(e: ComponentEvent) {
    }
    override fun componentHidden(e: ComponentEvent) {
    }
    companion object {
        @JvmStatic
        fun main() {
            val frame = JFrame()
            val frameHolder = korlibs.korge.ipc.KorgeIPCJPanel()
            frame.add(frameHolder)
            frame.addKeyListener(frameHolder)

            frame.preferredSize = Dimension(640, 480)
            frame.pack()
            frame.setLocationRelativeTo(null)

            frame.isVisible = true

        }
    }

    override fun focusGained(e: FocusEvent) = Unit
    override fun focusLost(e: FocusEvent) = Unit
}
