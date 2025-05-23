package com.soywiz.korge.intellij.actions

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.*
import com.soywiz.korge.intellij.util.*

class KorgeDocumentationAction : KorgeAction(), DumbAware {
	override fun actionPerformed(p0: AnActionEvent) {
		launchBrowserWithKorgeDocumention()
	}
}

fun launchBrowserWithKorgeDocumention() {
	launchBrowserWithUrl("https://docs.korge.org/")
}
