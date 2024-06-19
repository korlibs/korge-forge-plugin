package org.korge.intellij.plugin.toolWindow

import androidx.compose.runtime.*
import com.intellij.openapi.project.*
import com.intellij.openapi.wm.*
import com.intellij.ui.content.*
import korge.composable.*
import korlibs.korge.ipc.*

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
        //toolWindow.setTitleActions(listOf(
        //    object : KorgeAction("Refresh", "Refresh files", AllIcons.Actions.Refresh) {
        //        override fun actionPerformed(e: AnActionEvent) {
        //            iconCache.invalidateAll()
        //            setFolder(currentFolder)
        //        }
        //    },
        //    object : KorgeAction("Root", "Go Root", AllIcons.Actions.MoveUp) {
        //        override fun actionPerformed(e: AnActionEvent) {
        //            iconCache.invalidateAll()
        //            setFolder(baseDir)
        //        }
        //    }
        //))

        toolWindow.show()
    }
}
