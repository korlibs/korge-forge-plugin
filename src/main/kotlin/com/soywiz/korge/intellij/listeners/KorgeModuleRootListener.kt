package com.soywiz.korge.intellij.listeners

import com.intellij.openapi.module.ModuleTypeId
import com.intellij.openapi.roots.*
import com.soywiz.korge.intellij.*
import com.soywiz.korge.intellij.util.*
import org.jetbrains.kotlin.config.*

class KorgeModuleRootListener : ModuleRootListener {

    override fun beforeRootsChange(event: ModuleRootEvent) {
        println("Running KorgeGradleSyncListener.beforeRootsChange")
        //ModuleTypeId.JAVA_MODULE
        //KotlinModuleKind.DEFAULT
        val filePath = event.project.rootFile!!.canonicalPath!!
        println("Running KorgeGradleSyncListener.beforeRootsChange: filePath=$filePath")
        event.project.moduleManager.newModule(filePath, "")
    }

    override fun rootsChanged(event: ModuleRootEvent) {
        println("Running KorgeGradleSyncListener.rootsChanged")
        LibraryFixer.fixLibraries(event.project, false)
    }
}
