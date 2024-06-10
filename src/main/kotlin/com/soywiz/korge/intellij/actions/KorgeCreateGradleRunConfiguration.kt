package com.soywiz.korge.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.soywiz.korge.intellij.util.*

open class KorgeCreateGradleRunConfiguration : KorgeAction() {
    override fun actionPerformed(e: AnActionEvent) {
        createGradleRunConfiguration(e.project!!, "runJvmAutoreload")
    }
}
