package com.soywiz.korge.intellij.listeners

import com.intellij.openapi.roots.*
import com.soywiz.korge.intellij.util.*

class KorgeModuleRootListener : ModuleRootListener {

    override fun beforeRootsChange(event: ModuleRootEvent) {
        println("Running KorgeGradleSyncListener.beforeRootsChange")
        //ModuleTypeId.JAVA_MODULE
        //KotlinModuleKind.DEFAULT
        val filePath = event.project.rootFile?.canonicalPath
        println("Running KorgeGradleSyncListener.beforeRootsChange: filePath=$filePath")
        filePath?.let {
            try {
                //event.project.moduleManager.newModule(filePath, "")
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }

    override fun rootsChanged(event: ModuleRootEvent) {
        println("Running KorgeGradleSyncListener.rootsChanged")
        LibraryFixer.fixLibraries(event.project, false)
    }
}
