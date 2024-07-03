package com.soywiz.korge.intellij.listeners

import com.intellij.externalSystem.*
import com.intellij.ide.impl.*
import com.intellij.jarRepository.*
import com.intellij.openapi.application.*
import com.intellij.openapi.externalSystem.model.*
import com.intellij.openapi.externalSystem.model.project.*
import com.intellij.openapi.externalSystem.service.project.*
import com.intellij.openapi.externalSystem.service.project.manage.*
import com.intellij.openapi.module.*
import com.intellij.openapi.project.*
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ex.*
import com.intellij.openapi.projectRoots.*
import com.intellij.openapi.roots.*
import com.intellij.openapi.roots.libraries.*
import com.intellij.openapi.util.text.*
import com.intellij.openapi.vfs.*
import com.intellij.pom.java.*
import com.intellij.projectImport.*
import com.intellij.serialization.*
import com.intellij.util.containers.*
import org.jetbrains.idea.maven.utils.library.*
import org.jetbrains.idea.maven.utils.library.propertiesEditor.*
import org.jetbrains.kotlin.config.*
import org.jetbrains.kotlin.idea.base.codeInsight.CliArgumentStringBuilder.replaceLanguageFeature
import org.jetbrains.kotlin.idea.compiler.configuration.*
import org.jetbrains.kotlin.idea.facet.*
import org.jetbrains.kotlin.idea.framework.*
import org.jetbrains.kotlin.idea.gradle.configuration.*
import org.jetbrains.kotlin.idea.gradleJava.configuration.*
import org.jetbrains.kotlin.idea.gradleTooling.*
import org.jetbrains.kotlin.idea.projectConfiguration.*
import org.jetbrains.kotlin.idea.projectModel.*
import org.jetbrains.kotlin.platform.*
import org.jetbrains.kotlin.platform.js.*
import org.jetbrains.kotlin.platform.jvm.*
import org.jetbrains.plugins.gradle.model.*
import org.jetbrains.plugins.gradle.model.data.*
import org.jetbrains.plugins.gradle.service.project.*
import org.jetbrains.plugins.gradle.service.project.data.*
import java.io.*
import kotlin.io.path.*

// ProjectOpenProcessorBase
/**
 * [com.intellij.openapi.externalSystem.importing.AbstractOpenProjectProvider.openProject]
 * [org.jetbrains.plugins.gradle.service.project.open.GradleOpenProjectProvider.linkToExistingProject]
 * [org.jetbrains.kotlin.idea.gradleJava.configuration.KotlinGradleLibraryDataService]
 */
class KorgeProjectOpenProcessor : ProjectOpenProcessor() {
    override val name: String get() = "korge"

    override fun canOpenProject(file: VirtualFile): Boolean {
        println("KorgeProjectOpenProcessor.canOpenProject: file=$file")
        //return file.findChild("korge.yml") != null
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

        //reloadProject(project, virtualFile)
        //ProjectDataManagerImpl.getInstance().importData(generateDataNodeProjectData(virtualFile.toNioPath().absolutePathString()), project)


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
            //reloadProject(it, virtualFile)
            reloadProject2(it, virtualFile)
            //ProjectDataManagerImpl.getInstance().importData(generateDataNodeProjectData(virtualFile.toNioPath().absolutePathString()), project)
            return it
        }


        //ProjectManagerEx.getInstanceEx().openProjectAsync(pathToOpen, options)

        return null
    }

    companion object {
        //val basePlatforms = listOf(KotlinPlatform.JS, KotlinPlatform.JVM, KotlinPlatform.WASM)
        val basePlatforms = listOf(KotlinPlatform.JVM)
        val platforms = setOf(JsPlatforms.DefaultSimpleJsPlatform, JdkPlatform(JvmTarget.JVM_21), WasmPlatform)
        //val platform = TargetPlatform(platforms)
        val platform = TargetPlatform(setOf(JdkPlatform(JvmTarget.JVM_21)))
        val GRADLE_SYSTEM_ID = ProjectSystemId("GRADLE")


        fun generateDataNodeProjectData(projectPath: String): DataNode<ProjectData> {
            val projectData = ProjectData(ProjectSystemId.findById("GRADLE")!!, "korge-hello-world", projectPath, projectPath)
            val projectNode = DataNode<ProjectData>(ProjectKeys.PROJECT, projectData, null)
            val rootProjectPath = File(projectPath)

            fun file(path: String): File = File(rootProjectPath, path)

            //val srcJs = file("src@js")
            val srcJs = file("src")
            val resourcesJs = file("resources")

            fun <T : Any> DataNode<*>.add(key: Key<T>, value: T): DataNode<T> {
                val dataNode = DataNode(key, value, null)
                addChild(dataNode)
                return dataNode
            }

            val libJdk8 = LibraryData(GRADLE_SYSTEM_ID, "", false).also {
                val group = "org.jetbrains.kotlin"
                val name = "kotlin-stdlib"
                val version = "2.0.0"
                ///////////////
                val groupPath = group.replace('.', '/')
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
                moduleNode.add(KotlinTargetData.KEY, KotlinTargetData("common").also {
                    it.moduleIds = setOf("korge-hello-world:commonMain", "korge-hello-world:commonTest")
                    it.archiveFile = File(projectPath, "build/libs/korge-hello-world-common.klib")
                })
                //val gradleSourceSetDataCommon = GradleSourceSetData("korge-hello-world:commonMain", "korge-hello-world:commonMain", "korge-hello-world:commonMain", projectPath, projectPath)
                val gradleSourceSetDataJs = GradleSourceSetData("korge-hello-world:jsMain", "korge-hello-world:jsMain", "korge-hello-world:jsMain", projectPath, projectPath)
                moduleNode.add(KotlinGradleProjectData.KEY, KotlinGradleProjectData().also {
                    it.isHmpp = true
                })
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
                        sourceDirs = setOf(srcJs),
                        resourceDirs = setOf(resourcesJs),
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
                //moduleNode.add(ProjectKeys.MODULE_DEPENDENCY, ModuleDependencyData(gradleSourceSetDataJs, gradleSourceSetDataCommon).also {
                //    it.scope = DependencyScope.COMPILE
                //})
                moduleNode.add(ProjectKeys.LIBRARY_DEPENDENCY, LibraryDependencyData(gradleSourceSetDataJs, libJdk8, LibraryLevel.MODULE).also {
                    it.scope = DependencyScope.COMPILE
                })
                moduleNode.add(ProjectKeys.CONTENT_ROOT, ContentRootData(GRADLE_SYSTEM_ID, srcJs.absolutePath).also {
                    it.storePath(ExternalSystemSourceType.SOURCE, srcJs.absolutePath)
                })
            }

            // Libraries
            projectNode.add(ProjectKeys.LIBRARY, libJdk8)
            return projectNode
        }

        fun reloadProject2(project: Project, virtualFile: VirtualFile) {
            ProjectDataManagerImpl.getInstance().importData(generateDataNodeProjectData(virtualFile.toNioPath().absolutePathString()), project)
        }

        fun reloadProject(project: Project, virtualFile: VirtualFile) {
            KotlinSdkType.setUpIfNeeded()

            //KorgeSourceSetData("", "", "", "", "").set

            //project.customSdk = KotlinSdkType.INSTANCE.createSdk("Kotlin SDK")

            ApplicationManager.getApplication().runWriteAction {
                val sdk = ProjectJdkTable.getInstance().allJdks.firstOrNull()
                val projectManager = ProjectRootManager.getInstance(project)
                val modelsProvider = IdeModifiableModelsProviderImpl(project)

                val table = LibraryTablesRegistrar.getInstance().getLibraryTable(project)
                //modelsProvider.createLibrary("demo1")
                projectManager.projectSdk = sdk
                //project.customSdk = ProjectJdkTable.getInstance().allJdks.firstOrNull()

                //project.modifyModules {
                //}
                //project.save()

                //project.rootFile = virtualFile
                project.modifyModules {
                    for (module in project.modules) this.disposeModule(module)
                    val module = this.newNonPersistentModule("mymodule", ModuleTypeManager.getInstance().findByID("JAVA_MODULE").id)
                    //val facets = module.facetManager.createModifiableModel()
                    //val module = this.newModule(virtualFile.toNioPath().absolutePathString() + "/.idea/modules/${virtualFile.name}.iml", ModuleTypeManager.getInstance().findByID("JAVA_MODULE").id)

                    val modifiableRootModel = ModuleRootManager.getInstance(module).modifiableModel
                    //modifiableRootModel.sdk = KotlinSdkType.INSTANCE.createSdk("Kotlin SDK");

                    //KotlinSourceSetDataService.configureFacet()

                    val junit = table.createLibrary("junit").also {
                        it.modifiableModel.addJarDirectory("/tmp/junit.jar", false, OrderRootType.CLASSES)
                    }

                    //project.customSourceRootType = SourceKotlinRootType.sourceRootType
                    modifiableRootModel.addLibraryEntry(junit)
                    //modifiableRootModel.sdk = ProjectJdkTable.getInstance().allJdks.firstOrNull()
                    modifiableRootModel.inheritSdk()
                    //modifiableRootModel.addOrderEntry(hyb)

                    val contentEntry = modifiableRootModel.addContentEntry(virtualFile)

                    modifiableRootModel.commit()

                    //IdeUIModifiableModelsProvider(project, modifiableRootModel, ModulesConfigurator(), ModifiableArtifactModel())

                    //modifiableRootModel.commit()
                }

                //project.save()

                project.modifyModules {

                    for (module in this.modules) {

                        val kotlinFacet = module.getOrCreateFacet(modelsProvider, false, "GRADLE")
                        //val kotlinFacet = ideModule.getOrCreateFacet(modelsProvider, false, GradleConstants.SYSTEM_ID.id)
                        kotlinFacet.configureFacet(
                            compilerVersion = IdeKotlinVersion.get("2.0.0"),
                            platform = platform,
                            modelsProvider = modelsProvider,
                            hmppEnabled = true,
                            //pureKotlinSourceFolders = listOf("src"),
                            //dependsOnList = listOf(),
                            //additionalVisibleModuleNames = setOf()
                        )
                        module.sourceSetName = "commonMain"
                        module.hasExternalSdkConfiguration = false

                        kotlinFacet.configuration.settings.also {
                            it.apiLevel = LanguageVersion.KOTLIN_2_0
                            it.kind = KotlinModuleKind.DEFAULT
                        }
                        kotlinFacet.configuration.settings.compilerSettings?.also {
                            //it.version
                            it.additionalArguments = ""
                        }
                        kotlinFacet.configuration.settings.updateMergedArguments()

                        val facetSettings = KotlinFacetSettingsProvider.getInstance(module.project)?.getInitializedSettings(module)
                        if (facetSettings != null) {
                            val feature: LanguageFeature = LanguageFeature.Coroutines
                            val state = LanguageFeature.State.ENABLED
                            ModuleRootModificationUtil.updateModel(module) {
                                facetSettings.apiLevel = feature.sinceVersion
                                facetSettings.languageLevel = feature.sinceVersion
                                facetSettings.compilerSettings?.apply {
                                    additionalArguments = additionalArguments.replaceLanguageFeature(
                                        feature,
                                        state,
                                        getRuntimeLibraryVersion(module),
                                        separator = " ",
                                        quoted = false
                                    )
                                }
                            }
                        }

                        val libraryJarDescriptor = LibraryJarDescriptor.STDLIB_JAR

                        val scope = DependencyScope.COMPILE
                        val testScope = DependencyScope.TEST
                        val modifiableModelsProvider = IdeaModifiableModelsProvider()
                        modifiableModelsProvider.getModuleModifiableModel(module)?.let { modifiableRootModel ->
                            RepositoryLibrarySupport(module.project, RepositoryLibraryDescription.findDescription("org.jetbrains.kotlin", "kotlin-stdlib-jvm"), RepositoryLibraryPropertiesModel("2.0.0", true, true)).addSupport(module, modifiableRootModel, modifiableModelsProvider, scope)
                            RepositoryLibrarySupport(module.project, RepositoryLibraryDescription.findDescription("org.jetbrains.kotlin", "kotlin-test-junit"), RepositoryLibraryPropertiesModel("2.0.0", true, true)).addSupport(module, modifiableRootModel, modifiableModelsProvider, testScope)
                            RepositoryLibrarySupport(module.project, RepositoryLibraryDescription.findDescription("com.soywiz.korge", "korge-jvm"), RepositoryLibraryPropertiesModel("999.0.0.999", true, true)).addSupport(module, modifiableRootModel, modifiableModelsProvider, scope)
                            modifiableRootModel.commit()
                        }
                        //modifiableModelsProvider.commit

                        //RepositoryAddLibraryAction.addLibraryToModule(
                        //    RepositoryLibraryDescription.findDescription(libraryJarDescriptor.repositoryLibraryProperties),
                        //    module,
                        //    //kotlinStdlibVersion ?: KotlinPluginLayout.standaloneCompilerVersion.artifactVersion,
                        //    KotlinPluginLayout.standaloneCompilerVersion.artifactVersion,
                        //    scope,
                        //    /* downloadSources = */ true,
                        //    /* downloadJavaDocs = */ true
                        //)

                        val modifiableRootModel = ModuleRootManager.getInstance(module).modifiableModel
                        //val contentEntry = modifiableRootModel.addContentEntry(virtualFile)
                        val contentEntry = modifiableRootModel.contentEntries.first()
                        virtualFile.findChild("src")?.let {
                            //KotlinFacet
                            contentEntry.addSourceFolder(it, SourceKotlinRootType).also {
                                //it.packagePrefix = "commonMain"
                            }
                        }
                        //contentEntry.addExcludeFolder(".korge")
                        //contentEntry.addExcludeFolder(".idea")
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

                modelsProvider.commit()
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
