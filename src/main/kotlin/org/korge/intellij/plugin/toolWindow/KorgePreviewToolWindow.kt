package org.korge.intellij.plugin.toolWindow

import androidx.compose.runtime.*
import com.intellij.execution.*
import com.intellij.execution.actions.*
import com.intellij.execution.executors.*
import com.intellij.execution.impl.*
import com.intellij.execution.runners.*
import com.intellij.execution.ui.*
import com.intellij.icons.*
import com.intellij.openapi.Disposable
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
import org.korge.intellij.plugin.ipcpreview.*
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

class KorgePreviewPlayAction(val ipc: KorgeIPCInfo? = null, val panel: KorgeForgeIPCJPanel? = null) : KorgeAction("Play", "Play", AllIcons.Actions.Execute) {
    override fun actionPerformed(e: AnActionEvent) {
        KorgePreviewTool.play(e, ipc, panel)
    }
}

class KorgePreviewStopAction : KorgeAction("Stop", "Stop", AllIcons.Actions.Suspend) {
    override fun actionPerformed(e: AnActionEvent) {
        KorgePreviewTool.stop(e)
        //ProcessHandle.of(0L)
        //processHandler.destroyProcess();
        //val application =
        //ApplicationManager.getApplication().getService<InstantShutdown>(InstantShutdown::class.java).computeWithDisabledInstantShutdown<Boolean> {
        //    return true
        //}
    }
}

object KorgePreviewTool {
    fun play(e: AnActionEvent, ipc: KorgeIPCInfo? = null, panel: KorgeForgeIPCJPanel? = null) {
        val project: Project = e.project ?: return
        //e.project?.getService(Runcon::class.java)
        //RunAction
        //RunContextAction(DefaultRunExecutor()).actionPerformed(e)
        //GradleRunConfiguration(project, null, "runJvmAutoreload")
        val settings = createGradleRunConfiguration(project, "runJvmAutoreload", name = "run (preview)", select = false) {
            it.settings.env["KORGE_IPC"] = ipc?.path ?: KorgeIPCInfo.DEFAULT_PATH
        }
        //set.

        //val runManager = RunManager.getInstance(project)
        ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
        panel?.requestFocusInWindow()

        DefaultRunExecutor.getRunExecutorInstance()

        //// Find a specific run configuration by name
        //val configurationName = "YourConfigurationName"
        //val settings: RunnerAndConfigurationSettings? = runManager.allSettings.find { it.name == configurationName }
        //if (settings != null) {
        //    ProgramRunnerUtil.executeConfiguration(settings, DefaultRunExecutor.getRunExecutorInstance())
        //} else {
        //    // Handle the case where the configuration is not found
        //    println("Run configuration '$configurationName' not found.")
        //}
    }
    fun stop(e: AnActionEvent) {
        val project: Project = e.project ?: return

        val descriptors = getStoppableDescriptors(project)
        for (desc in descriptors) {
            //desc.second.
        }

        val manager = ExecutionManager.getInstance(project) as ExecutionManagerImpl

        fun retry(retryN: Int = 0) {
            StopAction().actionPerformed(e)

            //val runContentManager = RunContentManager.getInstance(project)
            val processes = manager.getRunningProcesses().toList()
            println("DESTROYING: processes=$processes")
            for (process in processes) {
                process.isProcessTerminated
                process.destroyProcess()
                if (retryN >= 2) {
                    (process as? KillableProcess)?.killProcess()
                }
                //process.waitFor()
            }
            println("  --> $processes")

            if (processes.isNotEmpty() && processes.any { !it.isProcessTerminated }) {
                com.soywiz.korge.intellij.invokeLater(1.seconds) {
                    retry(retryN + 1)
                }
            }
        }

        retry()
    }
}

class KorgePreviewToolWindow : ToolWindowFactory {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {

        //val panel = createComposableJPanel {
        //    MyFunc()
        //}

        val ipcInfo = KorgeIPCInfo()
        val panel = KorgeForgeIPCJPanel(KorgeIPCInfo())

        toolWindow.contentManager.addContent(ContentFactory.getInstance().createContent(panel, "", false))
        toolWindow.setTitleActions(listOf(
            KorgePreviewPlayAction(ipcInfo, panel),
            KorgePreviewStopAction(),
        ))

        toolWindow.show()
    }
}
