import org.jetbrains.intellij.tasks.*

plugins {
    java
    idea
    id("org.jetbrains.intellij") version "1.17.3"
    kotlin("jvm") version "2.0.0"
    kotlin("plugin.compose") version "2.0.0"
}

//val jvmVersion = JavaLanguageVersion.of(8)
//
//val compiler = javaToolchains.compilerFor {
//    languageVersion.set(jvmVersion)
//}

//project.tasks
//    .withType<org.jetbrains.kotlin.gradle.tasks.UsesKotlinJavaToolchain>()
//    .configureEach {
//        kotlinJavaToolchain.jdk.use(
//            compiler.get().metadata.installationPath.asFile.absolutePath,
//            jvmVersion
//        )
//    }


tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class).all {
    kotlinOptions {
        this.jvmTarget = "17"
    }
    //sourceCompatibility = JavaVersion.VERSION_17.toString()
    //targetCompatibility = JavaVersion.VERSION_17.toString()
//    kotlinOptions {
//        freeCompilerArgs = listOf("-XXLanguage:+InlineClasses")
//        jvmTarget = "1.8"
//    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

sourceSets {
    this.maybeCreate("main").apply {
        java {
            //srcDirs("korge-intellij-plugin/src/main/kotlin")
            //srcDirs("src/main/kotlin")
        }
        resources {
            //srcDirs("korge-intellij-plugin/src/main/resources")
            //srcDirs("src/main/resources")
        }
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    google()
}

intellij {
    // IntelliJ IDEA dependency
    version.set("IC-2024.1.1")
    // Bundled plugin dependencies
    plugins.addAll(
        "gradle",
        "java",
        "platform-images",
        "Kotlin",
        "gradle-java",
        "yaml"
    )

    pluginName.set("KorgePlugin")
    updateSinceUntilBuild.set(false)

    downloadSources.set(true)

    //sandboxDir.set(layout.projectDirectory.dir(".sandbox").toString())
}

tasks.withType(PublishPluginTask::class.java) {
    val publishToken = System.getenv("INTELLIJ_PUBLISH_TOKEN") ?: rootProject.findProperty("intellij.token.publish")
    if (publishToken != null) {
        this.token.set(publishToken.toString())
    }
}

tasks {
    // @TODO: Dependency substitution: https://docs.gradle.org/current/userguide/composite_builds.html

    //val compileKotlin by existing(Task::class) {
    //    dependsOn(gradle.includedBuild("korge-next").task(":publishJvmPublicationToMavenLocal"))
    //}

    val runIde by existing(org.jetbrains.intellij.tasks.RunIdeTask::class) {
        maxHeapSize = "4g"
        systemDir.set(File(System.getProperty("user.home"), ".korge/idea-system-dir"))
        //dependsOn(":korge-next:publishJvmPublicationToMavenLocal")
    }

    val extractPluginJars by creating(Sync::class) {
        dependsOn(buildPlugin)
        from(zipTree(File(rootDir, "build/distributions/KorgePlugin.zip")))
        into(File(rootDir, "build/distributions/KorgePlugin"))
    }

//    val runDebugTilemap by creating(JavaExec::class) {
//        //classpath = sourceSets.main.runtimeClasspath
//        classpath = sourceSets["main"].runtimeClasspath + configurations["idea"]
//
//        main = "com.soywiz.korge.intellij.editor.tile.MyTileMapEditorFrame"
//    }
//    val runUISample by creating(JavaExec::class) {
//        //classpath = sourceSets.main.runtimeClasspath
//        classpath = sourceSets["main"].runtimeClasspath + configurations["idea"]
//
//        main = "com.soywiz.korge.intellij.ui.UIBuilderSample"
//    }

    // ./gradlew runIde --dry-run

    //afterEvaluate { println((runIde.get() as Task).dependsOn.toList()) }
}

//println(gradle.includedBuilds)

val korgeVersion: String by project
val kotlinVersion: String by project

dependencies {
    implementation("com.soywiz.korge:korge-jvm:$korgeVersion")
    implementation("com.soywiz.korge:korge-ipc:$korgeVersion")
    implementation("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit:$kotlinVersion")
    implementation("org.jetbrains.compose.runtime:runtime:1.6.11")
}
