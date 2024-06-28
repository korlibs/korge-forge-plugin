package com.soywiz.korge.intellij.listeners

import com.intellij.ide.impl.*
import com.intellij.ide.projectWizard.*
import com.intellij.openapi.command.impl.DummyProject
import com.intellij.openapi.externalSystem.importing.*
import com.intellij.openapi.externalSystem.model.*
import com.intellij.openapi.project.*
import com.intellij.openapi.project.ex.*
import com.intellij.openapi.vfs.*
import com.intellij.projectImport.*

class KorgeProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String get() = "korge"

    override fun canOpenProject(file: VirtualFile): Boolean {
        return true
    }

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        println("KorgeProjectOpenProcessor.doOpenProject: virtualFile=$virtualFile, projectToClose=$projectToClose, forceOpenInNewFrame=$forceOpenInNewFrame")
        //return ProjectManagerEx.getInstanceEx().newProject(virtualFile.toNioPath(), OpenProjectTask {
        //    this.projectName = virtualFile.nameWithoutExtension
        //})
        return null
    }
}

/*
class KorgeProjectOpenProcessor : AbstractOpenProjectProvider() {
    companion object {
        val KORGE = ProjectSystemId("KORGE")
    }

    override val systemId: ProjectSystemId get() = KorgeProjectOpenProcessor.KORGE

    override fun canOpenProject(file: VirtualFile): Boolean {
        return true
    }

    override fun isProjectFile(file: VirtualFile): Boolean {
        if (file.name == "korge.yml") return true
        return false
    }

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        println("KorgeProjectOpenProcessor.doOpenProject: virtualFile=$virtualFile, projectToClose=$projectToClose, forceOpenInNewFrame=$forceOpenInNewFrame")
        return ProjectManagerEx.getInstanceEx().newProject(virtualFile.toNioPath(), OpenProjectTask {
            this.projectName = virtualFile.nameWithoutExtension
        })
    }
}
*/
/*

class GradleProjectOpenProcessor : ProjectOpenProcessor() {
  override val name: String
    get() = GradleBundle.message("gradle.name")

  override val icon: Icon
    get() = GradleIcons.Gradle

  override fun canOpenProject(file: VirtualFile): Boolean = canOpenGradleProject(file)

  override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
    return runUnderModalProgressIfIsEdt { openGradleProject(virtualFile, projectToClose, forceOpenInNewFrame) }
  }

  override suspend fun openProjectAsync(virtualFile: VirtualFile,
                                        projectToClose: Project?,
                                        forceOpenInNewFrame: Boolean): Project? {
    return openGradleProject(projectFile = virtualFile, projectToClose = projectToClose, forceOpenInNewFrame = forceOpenInNewFrame)
  }

  override fun canImportProjectAfterwards(): Boolean = true

  override suspend fun importProjectAfterwardsAsync(project: Project, file: VirtualFile) {
    linkAndRefreshGradleProject(file.path, project)
  }
}

 */
