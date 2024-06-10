package com.soywiz.korge.intellij.util

import com.intellij.openapi.actionSystem.*
import javax.swing.*

abstract class KorgeAction(
    text: String? = null,
    description: String? = null,
    icon: Icon? = null,
    protected val updateThread: ActionUpdateThread = ActionUpdateThread.EDT,
) : AnAction(
    text, description, icon,
) {
    override fun getActionUpdateThread(): ActionUpdateThread = updateThread
}
