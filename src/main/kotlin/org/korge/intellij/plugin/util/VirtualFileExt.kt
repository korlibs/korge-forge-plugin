// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

import com.intellij.openapi.vfs.VirtualFile

fun VirtualFile?.isAncestorOf(child: VirtualFile?, includeThis: Boolean = true): Boolean {
  val parent = this
  if (parent == null || child == null) return false
  if (includeThis && this == child) return true

  val parentPath = parent.path
  val childPath = child.path

  return childPath.startsWith("$parentPath/")
}