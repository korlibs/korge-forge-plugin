// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

import java.awt.Dimension

class ScaleMode(
  val name: String? = null,
  val transform: (item: Size, container: Size) -> Size
) {
  override fun toString(): String = "ScaleMode($name)"

  companion object {
    val COVER: ScaleMode = ScaleMode("COVER") { i, c -> i * (c / i).maxComponent() }
    val SHOW_ALL: ScaleMode = ScaleMode("SHOW_ALL") { i, c -> i * (c / i).minComponent() }
    val FIT: ScaleMode get() = SHOW_ALL
    val FILL: ScaleMode get() = EXACT
    val EXACT: ScaleMode = ScaleMode("EXACT") { i, c -> c }
    val NO_SCALE: ScaleMode = ScaleMode("NO_SCALE") { i, c -> i }
  }
}

data class Size(val width: Double, val height: Double) {
  constructor(width: Number, height: Number) : this(width.toDouble(), height.toDouble())
  operator fun times(other: Size): Size = Size(width * other.width, height * other.height)
  operator fun div(other: Size): Size = Size(width / other.width, height / other.height)
  operator fun times(other: Double): Size = Size(width * other, height * other)
  operator fun div(other: Double): Size = Size(width / other, height / other)
  fun maxComponent(): Double = maxOf(width, height)
  fun minComponent(): Double = minOf(width, height)
}

fun Size.toDimension(): Dimension = Dimension(width.toInt(), height.toInt())
fun Dimension.toSize(): Size = Size(width.toDouble(), height.toDouble())