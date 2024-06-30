package com.soywiz.korge.intellij.actions

import com.intellij.ide.impl.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.module.ModifiableModuleModel
import com.intellij.openapi.project.*
import com.intellij.openapi.project.ex.*
import com.intellij.openapi.startup.*
import com.soywiz.korge.intellij.*
import com.soywiz.korge.intellij.util.*


class KorgeLoadProject : KorgeAction(), DumbAware {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null) return
        val modules = e.project!!.moduleManager.modules.toList()
        println("KorgeLoadProject.modules: $modules")
        //val projectManager = ProjectManager.getInstance()
        //val project = projectManager.loadAndOpenProject(projectRoot.getPath())
        //projectManager.createProject()
        //e.
        //ProjectManagerEx.getInstanceEx().newProject(OpenProjectTask {
        //})
    }
}
