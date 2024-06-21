package org.korge.intellij.plugin.ipcpreview

import com.intellij.openapi.*
import com.intellij.ui.*
import com.intellij.util.ui.*
import korlibs.korge.ipc.*
import korlibs.time.*
import java.awt.*
import java.awt.event.*
import java.awt.image.*
import javax.swing.*
import kotlin.jvm.optionals.*

// With JPanel isFocusable doesn't work as expected
class KorgeForgeIPCJPanel(val ipcInfo: KorgeIPCInfo = KorgeIPCInfo()) : JButton(), MouseListener, MouseMotionListener, MouseWheelListener, KeyListener, HierarchyListener, FocusListener, ComponentListener, Disposable {
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

    var ipc: KorgeIPC? = null
    var checkProcessTimer = Stopwatch().start()
    fun doRenderLoop(): AutoCloseable {
        sendEv(IPCEvent.RESIZE, width, height)

        val timer = Timer(1000 / 100) {
            try {
                if (checkProcessTimer.elapsed >= 0.25.seconds) {
                    checkProcessTimer.restart()
                    if (lastFrame != null) {
                        if (ProcessHandle.of(lastFrame!!.pid.toLong()).getOrNull()?.isAlive != true) {
                            ipc?.setFrame(IPCFrame(-1, 0, 0, IntArray(0)))
                            lastFrame = null
                            image = null
                        }
                    }
                }

                readFrame()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
            //Thread.sleep(1L)
            //SwingUtilities.invokeLater(::func)
        }.also { it.isRepeats = true; it.start() }

        println("RENDERLOOP")
        return korlibs.io.lang.AutoCloseable {
            println("/RENDERLOOP")
            //running = false
            timer.stop()
        }
    }

    var renderLoop: AutoCloseable? = null

    override fun hierarchyChanged(e: HierarchyEvent) {
        if ((e.changeFlags and HierarchyEvent.SHOWING_CHANGED.toLong()) != 0L) {
            if (isShowing) {
                if (ipc == null) {
                    ipc = ipcInfo.createIPC()
                    ipc?.resetEvents()
                    repeat(2) { ipc?.writeEvent(IPCEvent(type = IPCEvent.BRING_BACK)) }
                }

                sendEv(IPCEvent.BRING_BACK)
                renderLoop?.close()
                renderLoop = doRenderLoop()
            } else {
                renderLoop?.close()
                renderLoop = null
                if (ipc != null) {
                    ipc?.close()
                    ipc = null
                }
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
            g.font = JBUI.Fonts.label()
            g.drawString("Not running yet... Press the play icon to start", 25, 25)
        }
    }

    fun rgbaToBgra(v: Int): Int = ((v shl 16) and 0x00FF0000) or ((v shr 16) and 0x000000FF) or (v and 0xFF00FF00.toInt())

    fun readFrame() {
        val frameId = ipc?.getFrameId()
        if (frameId == currentFrameId) {
            return
        } // Do not update
        val frame = ipc?.getFrame() ?: return
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

    //val devicePixelRatio: Double get() {
    //    return JBUI.pixScale().toDouble()
    //    //val defaultScreenDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice
    //    //val defaultConfiguration = defaultScreenDevice.defaultConfiguration
    //    //val defaultTransform = defaultConfiguration.defaultTransform
    //    //return defaultTransform.scaleX
    //}
    //val Component.devicePixelRatio: Double get() {
    //    if (GraphicsEnvironment.isHeadless()) {
    //        return 1.0
    //    } else {
    //        // transform
    //        // https://stackoverflow.com/questions/20767708/how-do-you-detect-a-retina-display-in-java
    //        val config = graphicsConfiguration
    //            ?: GraphicsEnvironment.getLocalGraphicsEnvironment().defaultScreenDevice.defaultConfiguration
    //        return config.defaultTransform.scaleX
    //    }
    //}

    private fun sendEv(type: Int, p0: Int = 0, p1: Int = 0, p2: Int = 0, p3: Int = 0) {
        ipc?.writeEvent(IPCEvent(type = type, p0 = p0, p1 = p1, p2 = p2, p3 = p3))
    }
    private fun sendEv(type: Int, e: KeyEvent) = sendEv(type = type, p0 = e.keyCode, p1 = e.keyChar.code)
    private fun sendEv(type: Int, e: MouseEvent) {
        //val ratio = 1.0 / devicePixelRatio
        val ratio = JBUI.pixScale().toDouble()
        //println("ratio=$ratio, jb=${JBUI.pixScale().toDouble()}")
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
        //println("componentResized: width=$width, height=$height")
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
    override fun dispose() {
        //println("DISPOSE!! (do nothing)")
        //ipc?.close()
        //ipc = null
        //renderLoop?.close()
        //renderLoop = null
    }
}
