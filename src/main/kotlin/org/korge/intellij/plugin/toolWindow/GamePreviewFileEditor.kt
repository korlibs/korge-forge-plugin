package org.korge.intellij.plugin.toolWindow

import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.util.*
import com.intellij.util.ui.*
import korlibs.korge.ipc.*
import org.korge.intellij.plugin.ipcpreview.*
import java.beans.*
import javax.swing.*

class GamePreviewFileEditor(val project: com.intellij.openapi.project.Project) : UserDataHolderBase(), FileEditor {

    //val ipc = KorgeIPC().also {
    //    it.writeEvent(korlibs.korge.ipc.IPCEvent(type = korlibs.korge.ipc.IPCEvent.BRING_BACK))
    //}
    val ipcInfo = KorgeIPCInfo()
    val panel by lazy { KorgeForgeIPCJPanel(ipcInfo) }
    val toolBar by lazy {
        ActionManager.getInstance().createActionToolbar(ActionPlaces.TEXT_EDITOR_WITH_PREVIEW, DefaultActionGroup().also {
            it.add(KorgePreviewPlayAction(ipcInfo))
            it.add(KorgePreviewStopAction())
        }, true).also {
            it.targetComponent = panel
        }
    }
    //val toolBar = EditorHeaderComponent().also {
    //    val actionGroup: ActionGroup = createLeftToolbarActionGroup()
    //    return if (actionGroup != null) {
    //
    //    } else {
    //        null
    //    }
    //    it.layout = GridBagLayout()
    //    //object : ActionToolbarImpl("MyPlace")
    //    it.add(JButton("Play").onClick {
    //        KorgePreviewTool.play(AnActionEvent.createFromInputEvent(it, "", null, project.dataContext), ipc, panel)
    //    })
    //    it.add(JButton("Stop").onClick {
    //        KorgePreviewTool.stop(AnActionEvent.createFromInputEvent(it, "", null, project.dataContext))
    //    })
    //}
    val full by lazy {
        JBUI.Panels.simplePanel(panel).addToTop(toolBar.component)
    }

    override fun dispose() {
        //JBToolBar
        panel.dispose()
    }
    override fun getComponent(): JComponent = full
    override fun getPreferredFocusedComponent(): JComponent? = panel
    override fun getName(): String = "GamePreviewFileEditor"

    override fun setState(state: FileEditorState) {
    }

    override fun isModified(): Boolean {
        return false
    }

    override fun isValid(): Boolean {
        return true
    }

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }
}
