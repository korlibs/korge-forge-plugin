import org.jetbrains.intellij.platform.gradle.extensions.*

pluginManagement { repositories {  mavenLocal(); mavenCentral(); google(); gradlePluginPortal()  }  }

// @TODO: Dependency substitution: https://docs.gradle.org/current/userguide/composite_builds.html

plugins {
    id("org.jetbrains.intellij.platform.settings") version "2.0.1"
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.8.0")
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenLocal()
        mavenCentral()
        google()
        intellijPlatform {
            defaultRepositories()
        }
    }
}

//plugins {
//    //id("com.soywiz.kproject.settings") version "0.0.1-SNAPSHOT"
//    id("com.soywiz.kproject.settings") version "0.3.0"
//}
//
//kproject("./deps")

rootProject.name = "korge-forge-plugin"

