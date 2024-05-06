package org.korge.intellij.plugin.util

import com.intellij.ide.*
import com.intellij.openapi.wm.*

val com.intellij.openapi.project.Project.jframe get() = WindowManager.getInstance().getFrame(this)
val com.intellij.openapi.project.Project.dataContext get() = DataManager.getInstance().getDataContext(jframe)
