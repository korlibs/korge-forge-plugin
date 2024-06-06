package com.soywiz.korge.intellij.listeners

import com.intellij.openapi.roots.*
import com.soywiz.korge.intellij.util.*

class KorgeModuleRootListener : ModuleRootListener {

    override fun beforeRootsChange(event: ModuleRootEvent) {
    }

    override fun rootsChanged(event: ModuleRootEvent) {
        println("Running KorgeGradleSyncListener")
        LibraryFixer.fixLibraries(event.project, false)
    }
}
