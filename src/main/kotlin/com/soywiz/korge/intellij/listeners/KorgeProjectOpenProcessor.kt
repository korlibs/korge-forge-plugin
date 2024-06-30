package com.soywiz.korge.intellij.listeners

import com.intellij.ide.impl.*
import com.intellij.openapi.application.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.project.ex.*
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.vfs.*
import com.intellij.projectImport.*
import org.jetbrains.jps.model.java.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.base.projectStructure.*
import org.jetbrains.kotlin.idea.framework.*
import kotlin.io.path.*

// ProjectOpenProcessorBase
/**
 * [com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider.openProject]
 * [org.jetbrains.plugins.gradle.service.project.open.GradleOpenProjectProvider.linkToExistingProject]
 */
class KorgeProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String get() = "korge"

    override fun canOpenProject(file: VirtualFile): Boolean {
        println("KorgeProjectOpenProcessor.canOpenProject: file=$file")
        return true
    }

    override fun doOpenProject(virtualFile: VirtualFile, projectToClose: Project?, forceOpenInNewFrame: Boolean): Project? {
        projectToClose?.let { project ->
            invokeAndWaitIfNeeded {
                ProjectManagerEx.getInstanceEx().forceCloseProject(project)
            }
        }
        println("KorgeProjectOpenProcessor.doOpenProject: virtualFile=$virtualFile, projectToClose=$projectToClose, forceOpenInNewFrame=$forceOpenInNewFrame")

        //val project = ProjectManager.getInstance().createProject(virtualFile.name, virtualFile.toNioPath().absolutePathString())
        val options = OpenProjectTask {
            this.projectToClose = projectToClose
            this.forceOpenInNewFrame = forceOpenInNewFrame
            this.projectName = virtualFile.nameWithoutExtension
            this.isNewProject = true
            //this.beforeOpen { true }
        }
        val project = ProjectManagerEx.getInstanceEx().newProject(virtualFile.toNioPath(), options) ?: error("Can't create project")

        reloadProject(project, virtualFile)


        //return ProjectManagerEx.getInstanceEx().newProject(virtualFile.toNioPath(), OpenProjectTask {
        //    this.projectName = virtualFile.nameWithoutExtension
        //    val moduleManager = ModuleManager.getInstance(project!!)
//
        //})
        //return null
        //val project = runBlockingModalWithRawProgressReporter(ModalTaskOwner.guess(), IdeBundle.message("title.open.project")) {
        //    if (importToProject) {
        //        options = options.copy(beforeOpen = { project -> importToProject(project, projectToClose, wizardContext) })
        //    }
        //    ProjectManagerEx.getInstanceEx().openProjectAsync(pathToOpen, options)
        //}
        //ProjectUtil.updateLastProjectLocation(pathToOpen)
        //return project

        println(" --> doOpenProject: $project")


        ProjectManagerEx.getInstanceEx().openProject(virtualFile.toNioPath(), options)?.let {
            reloadProject(it, virtualFile)
            return it
        }


        //ProjectManagerEx.getInstanceEx().openProjectAsync(pathToOpen, options)

        return null
    }

    companion object {
        fun reloadProject(project: Project, virtualFile: VirtualFile) {
            KotlinSdkType.setUpIfNeeded()



            //project.customSdk = KotlinSdkType.INSTANCE.createSdk("Kotlin SDK")
            project.customSdk = ProjectJdkTable.getInstance().allJdks.firstOrNull()
            ApplicationManager.getApplication().runWriteAction {
                //project.rootFile = virtualFile
                project.modifyModules {
                    for (module in project.modules) this.disposeModule(module)
                    val module = this.newNonPersistentModule("mymodule", ModuleTypeManager.getInstance().findByID("JAVA_MODULE").id)
                    //val module = this.newModule(virtualFile.toNioPath().absolutePathString() + "/module.iml", ModuleTypeManager.getInstance().findByID("JAVA_MODULE").id)

                    val modifiableRootModel = ModuleRootManager.getInstance(module).modifiableModel
                    //modifiableRootModel.sdk = KotlinSdkType.INSTANCE.createSdk("Kotlin SDK");
                    val table = LibraryTablesRegistrar.getInstance().getLibraryTable(project)
                    val junit = table.createLibrary("junit").also {
                        it.modifiableModel.addJarDirectory("/tmp/junit.jar", false, OrderRootType.CLASSES)
                    }
                    modifiableRootModel.addLibraryEntry(junit)
                    modifiableRootModel.inheritSdk()
                    //modifiableRootModel.addOrderEntry(OrderEntry)
                    val contentEntry = modifiableRootModel.addContentEntry(virtualFile)
                    virtualFile.findChild("src")?.let {
                        contentEntry.addSourceFolder(it, SourceKotlinRootType).also {
                            //it.packagePrefix = "commonMain"
                        }
                    }
                    virtualFile.findChild("resources")?.let {
                        contentEntry.addSourceFolder(it, ResourceKotlinRootType)
                    }
                    virtualFile.findChild("test")?.let {
                        contentEntry.addSourceFolder(it, TestSourceKotlinRootType)
                    }
                    virtualFile.findChild("testresources")?.let {
                        contentEntry.addSourceFolder(it, TestResourceKotlinRootType)
                    }
                    modifiableRootModel.commit()
                }
            }

        }
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
