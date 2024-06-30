package com.soywiz.korge.intellij.listeners

import com.intellij.externalSystem.JavaProjectData
import com.intellij.externalSystem.MavenRepositoryData
import com.intellij.ide.impl.*
import com.intellij.openapi.application.*
import com.intellij.openapi.externalSystem.model.*
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.service.project.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.*
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.roots.ui.configuration.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.pom.java.*
import com.intellij.projectImport.*
import com.intellij.serialization.*
import com.intellij.util.containers.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.base.projectStructure.*
import org.jetbrains.kotlin.idea.compiler.configuration.*
import org.jetbrains.kotlin.idea.facet.*
import org.jetbrains.kotlin.idea.framework.*
import org.jetbrains.kotlin.idea.gradle.configuration.*
import org.jetbrains.kotlin.idea.gradleJava.configuration.*
import org.jetbrains.kotlin.idea.gradleTooling.*
import org.jetbrains.kotlin.idea.projectModel.*
import org.jetbrains.kotlin.platform.*
import org.jetbrains.kotlin.platform.js.*
import org.jetbrains.kotlin.platform.jvm.*
import org.jetbrains.plugins.gradle.model.*
import org.jetbrains.plugins.gradle.model.data.GradleSourceSetData
import org.jetbrains.plugins.gradle.service.project.*
import org.jetbrains.plugins.gradle.service.project.data.*
import org.jetbrains.plugins.gradle.util.*
import java.io.*

// ProjectOpenProcessorBase
/**
 * [com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider.openProject]
 * [org.jetbrains.plugins.gradle.service.project.open.GradleOpenProjectProvider.linkToExistingProject]
 */
class KorgeProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String get() = "korge"

    override fun canOpenProject(file: VirtualFile): Boolean {
        println("KorgeProjectOpenProcessor.canOpenProject: file=$file")
        //return true
        return false
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
        val basePlatforms = listOf(KotlinPlatform.JS, KotlinPlatform.JVM, KotlinPlatform.WASM)
        val platforms = setOf(JsPlatforms.DefaultSimpleJsPlatform, JdkPlatform(JvmTarget.JVM_21), WasmPlatform)
        val platform = TargetPlatform(platforms)


        fun generateDataNodeProjectData(): DataNode<ProjectData> {
            val projectPath = "/Users/soywiz/IdeaProjects/untitled1"
            val projectData = ProjectData(ProjectSystemId.findById("GRADLE")!!, "korge-hello-world", projectPath, projectPath)
            val projectNode = DataNode<ProjectData>(ProjectKeys.PROJECT, projectData, null)

            fun file(path: String): File = File(projectPath, path)

            val GRADLE_SYSTEM_ID = ProjectSystemId("GRADLE")

            fun <T : Any> DataNode<*>.add(key: Key<T>, value: T): DataNode<T> {
                val dataNode = DataNode(key, value, null)
                addChild(dataNode)
                return dataNode
            }

            val libJdk8 = LibraryData(GRADLE_SYSTEM_ID, "", false).also {
                var group = "org.jetbrains.kotlin"
                var name = "kotlin-stdlib-jdk8"
                val groupPath = group.replace('.', '/')
                var version = "1.5.21"
                val full = "$group:$name:$version"
                it.setGroup(group)
                it.artifactId = name
                it.version = version
                it.externalName = full
                it.internalName = full
                it.addPath(LibraryPathType.BINARY, "/Users/soywiz/.m2/repository/$groupPath/$name/$version/$name-$version.jar")
                it.addPath(LibraryPathType.SOURCE, "/Users/soywiz/.m2/repository/$groupPath/$name/$version/$name-$version-sources.jar")
            }

            //projectNode.addChild(DataNode(ExternalSystemOperationDescriptor.OPERATION_DESCRIPTOR_KEY, ExternalSystemOperationDescriptor(3L), projectNode))
            projectNode.add(JavaProjectData.KEY, JavaProjectData(GRADLE_SYSTEM_ID, "$projectPath/build/classes", LanguageLevel.JDK_21, "21"))
            projectNode.add(ProjectSdkData.KEY, ProjectSdkData("21"))
            projectNode.add(MavenRepositoryData.KEY, MavenRepositoryData(GRADLE_SYSTEM_ID, "maven", "https://plugins.gradle.org/m2/"))
            projectNode.add(MavenRepositoryData.KEY, MavenRepositoryData(GRADLE_SYSTEM_ID, "MavenRepo", "https://repo.maven.apache.org/maven2/"))
            projectNode.add(MavenRepositoryData.KEY, MavenRepositoryData(GRADLE_SYSTEM_ID, "MavenLocal", "file:/Users/soywiz/.m2/repository/"))
            projectNode.add(ExternalProjectDataCache.KEY, DefaultExternalProject().also {
                it.id = "korge-hello-world"
                it.path = ":"
                it.identityPath = ":"
                it.name = "korge-hello-world"
                it.qName = "korge-hello-world"
                it.projectDir = File(projectPath)
                it.buildDir = File(projectPath, "build")
                it.buildFile = File(projectPath, "build.gradle.kts")
            })

            projectNode.add(ProjectKeys.MODULE, ModuleData("korge-hello-world", GRADLE_SYSTEM_ID, "JAVA_MODULE", "korge-hello-world", "korge-hello-world", projectPath)).also { moduleNode ->
                moduleNode.add(KotlinTargetData.KEY, KotlinTargetData("js").also {
                    it.moduleIds = setOf("korge-hello-world:jsMain", "korge-hello-world:jsTest")
                    it.archiveFile = File(projectPath, "build/libs/korge-hello-world-js.klib")
                })
                val gradleSourceSetDataCommon = GradleSourceSetData("korge-hello-world:commonMain", "korge-hello-world:commonMain", "korge-hello-world:commonMain", projectPath, projectPath)
                val gradleSourceSetDataJs = GradleSourceSetData("korge-hello-world:jsMain", "korge-hello-world:jsMain", "korge-hello-world:jsMain", projectPath, projectPath)
                moduleNode.add(GradleSourceSetData.KEY, gradleSourceSetDataJs).also { sourceSetNode ->
                    val settings = KotlinLanguageSettingsImpl(
                        languageVersion = "2.0",
                        apiVersion = "2.0",
                        isProgressiveMode = false,
                        enabledLanguageFeatures = setOf(),
                        optInAnnotationsInUse = setOf(),
                        compilerPluginArguments = arrayOf(),
                        compilerPluginClasspath = setOf(),
                        freeCompilerArgs = arrayOf()
                    )

                    val commonSourceSet = KotlinSourceSetImpl(
                        name = "commonMain",
                        languageSettings = settings,
                        sourceDirs = setOf(file("src")),
                        resourceDirs = setOf(file("resources")),
                        regularDependencies = arrayOf(),
                        intransitiveDependencies = arrayOf(),
                        declaredDependsOnSourceSets = setOf(),
                        allDependsOnSourceSets = setOf(),
                        additionalVisibleSourceSets = setOf(),
                        androidSourceSetInfo = null,
                        actualPlatforms = KotlinPlatformContainerImpl().also { it.pushPlatforms(basePlatforms) },
                        isTestComponent = false,
                        extras = IdeaKotlinExtras.empty(),
                    )

                    val jsSourceSet = KotlinSourceSetImpl(
                        name = "jsMain",
                        languageSettings = settings,
                        sourceDirs = setOf(file("src@js")),
                        resourceDirs = setOf(file("resources@js")),
                        regularDependencies = arrayOf(),
                        intransitiveDependencies = arrayOf(),
                        declaredDependsOnSourceSets = setOf(),
                        allDependsOnSourceSets = setOf(),
                        additionalVisibleSourceSets = setOf(),
                        androidSourceSetInfo = null,
                        actualPlatforms = KotlinPlatformContainerImpl().also { it.pushPlatforms(basePlatforms) },
                        isTestComponent = false,
                        extras = IdeaKotlinExtras.empty(),
                    )

                    val compilation = KotlinCompilationImpl(
                        name = "jsMain",
                        allSourceSets = setOf(commonSourceSet, jsSourceSet),
                        declaredSourceSets = setOf(commonSourceSet, jsSourceSet),
                        dependencies = arrayOf(),
                        output = KotlinCompilationOutputImpl(setOf(), null, null),
                        compilerArguments = listOf(),
                        kotlinTaskProperties = KotlinTaskPropertiesImpl(null, null, null, null),
                        nativeExtensions = null,
                        associateCompilations = setOf(),
                        extras = IdeaKotlinExtras.empty(),
                        isTestComponent = false,
                        archiveFile = null,
                    )
                    sourceSetNode.add(KotlinSourceSetData.KEY, KotlinSourceSetData(KotlinSourceSetInfo(compilation)))
                }
                moduleNode.add(KotlinOutputPathsData.KEY, KotlinOutputPathsData(MultiMap<ExternalSystemSourceType, String>().also {
                    it.putValue(ExternalSystemSourceType.SOURCE, "build/classes/kotlin/js/main")
                    it.putValue(ExternalSystemSourceType.RESOURCE, "build/processedResources/js/main")
                }))
                moduleNode.add(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(gradleSourceSetDataJs, gradleSourceSetDataCommon).also {
                    it.scope = DependencyScope.COMPILE
                })
                moduleNode.add(ProjectKeys.LIBRARY_DEPENDENCY, LibraryDependencyData(gradleSourceSetDataJs, libJdk8, LibraryLevel.PROJECT).also {
                    it.scope = DependencyScope.COMPILE
                })
                moduleNode.add(ProjectKeys.CONTENT_ROOT, ContentRootData(GRADLE_SYSTEM_ID, file("src@js").absolutePath).also {
                    it.storePath(ExternalSystemSourceType.SOURCE, file("src@js").absolutePath)
                })
            }

            // Libraries
            projectNode.add(ProjectKeys.LIBRARY, libJdk8)
            return projectNode
        }

        fun reloadProject(project: Project, virtualFile: VirtualFile) {
            KotlinSdkType.setUpIfNeeded()

            //KorgeSourceSetData("", "", "", "", "").set

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

                    //KotlinSourceSetDataService.configureFacet()

                    val modelsProvider = IdeModifiableModelsProviderImpl(project)
                    val kotlinFacet = module.getOrCreateFacet(modelsProvider, true, "gradle", false)
                    kotlinFacet.configureFacet(
                        IdeKotlinVersion.fromLanguageVersion(LanguageVersion.KOTLIN_2_0),
                        platform,
                        modelsProvider,
                        false,
                        listOf("src"),
                        listOf(),
                        setOf()
                    )

                    modifiableRootModel.addLibraryEntry(junit)
                    modifiableRootModel.inheritSdk()
                    //modifiableRootModel.addOrderEntry(OrderEntry)
                    val contentEntry = modifiableRootModel.addContentEntry(virtualFile)
                    virtualFile.findChild("src")?.let {
                        //KotlinFacet
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

class KorgeSourceSetData @PropertyMapping("id", "externalName", "internalName", "moduleFileDirectoryPath", "externalConfigPath") constructor(
    id: String,
    externalName: String,
    internalName: String,
    moduleFileDirectoryPath: String,
    externalConfigPath: String
) : ModuleData(
    id, SYSTEM_ID, GradleProjectResolverUtil.getDefaultModuleTypeId(),
    externalName, internalName,
    moduleFileDirectoryPath, externalConfigPath
) {
    init {
        moduleName = sourceSetName!!
    }

    override fun getIdeGrouping(): String = super.getIdeGrouping() + ":" + sourceSetName
    override fun getIdeParentGrouping(): String? = super.getIdeGrouping()
    private val sourceSetName: String? get() = StringUtil.substringAfterLast(externalName, ":")

    companion object {
        val SYSTEM_ID = ProjectSystemId("KORGE")
        val KEY: Key<KorgeSourceSetData> = Key.create(KorgeSourceSetData::class.java, ProjectKeys.MODULE.processingWeight + 1)
    }
}
