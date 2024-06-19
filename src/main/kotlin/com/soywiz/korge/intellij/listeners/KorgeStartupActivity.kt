package com.soywiz.korge.intellij.listeners

import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.intellij.openapi.startup.*
import com.intellij.openapi.wm.*

class KorgeStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        println("KorgeStartupActivity.execute")
        ApplicationManager.getApplication().invokeLater {
            val toolbox = ToolWindowManager.getInstance(project).getToolWindow("Korge Resources")
            println("KorgeStartupActivity.execute: toolbox=$toolbox")
            //toolbox?.isVisible = true
            //toolbox?.anchor = ToolWindowAnchor.RIGHT
            if (toolbox != null) {
                if (!toolbox.isVisible) {
                    toolbox.show()
                    toolbox.hide()
                }
            }
            //toolbox?.activate(null)
        }
        ApplicationManager.getApplication().invokeLater {
            val toolbox = ToolWindowManager.getInstance(project).getToolWindow("Korge Preview")
            println("KorgeStartupActivity.execute: toolbox=$toolbox")
            //toolbox?.isVisible = true
            //toolbox?.anchor = ToolWindowAnchor.RIGHT
            if (toolbox != null) {
                if (!toolbox.isVisible) {
                    toolbox.show()
                    toolbox.hide()
                }
            }
            //toolbox?.activate(null)
        }
    }
}
