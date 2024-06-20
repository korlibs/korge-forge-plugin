package com.soywiz.korge.intellij.listeners

import com.intellij.ide.plugins.*
import com.intellij.notification.*
import com.intellij.openapi.application.*
import com.intellij.openapi.extensions.*
import com.intellij.openapi.project.*
import com.intellij.openapi.startup.*
import com.intellij.openapi.wm.*
import com.soywiz.korge.intellij.actions.*
import com.soywiz.korge.intellij.updater.*

class KorgeStartupActivity : ProjectActivity {
    override suspend fun execute(project: Project) {
        //notificationManager.notify("", notificationMessage, project) { it.setSuggestionType(true).addActions(notificationActions as Collection<AnAction>) }

        println("KorgeStartupActivity.execute: korgePlugin.version=${KORGE_FORGE_VERSION}")

        KorgeCheckForUpdatesOnceADay.execute(project)

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
