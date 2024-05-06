// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

class Signal<T> {
  private val listeners = arrayListOf<(T) -> Unit>()

  fun clear() {
    listeners.clear()
  }

  fun add(listener: (T) -> Unit) {
    listeners += listener
  }

  operator fun invoke(item: T) {
    for (listener in listeners.toList()) {
      listener(item)
    }
  }
}