// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

import java.awt.Component
import java.awt.image.BufferedImage
import javax.swing.Icon
import javax.swing.ImageIcon

fun Icon.toImage(component: Component? = null): BufferedImage {
  val icon = this
  //val image = UIUtil.createImage(component, width, height, BufferedImage.TYPE_INT_ARGB)
  return BufferedImage(icon.iconWidth, icon.iconHeight, BufferedImage.TYPE_INT_ARGB).also {
    val graphics = it.graphics
    icon.paintIcon(component, graphics, 0, 0)
    graphics.dispose()
  }
}

fun Icon.renderCentered(width: Int, height: Int, component: Component? = null): Icon {
  return ImageIcon(this.toImage(component).getScaledInstanceNew(Size(width, height), ScaleMode.SHOW_ALL))
}
