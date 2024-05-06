// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.korge.intellij.plugin.util

import java.awt.Image
import java.awt.image.BufferedImage

val Image.width: Int get() = getWidth(null)
val Image.height: Int get() = getHeight(null)

fun Image.getScaledInstanceNew(size: Size, scaleMode: ScaleMode): BufferedImage {
  val image = this
  val (outWidth, outHeight) = scaleMode.transform(Size(image.width, image.height), size)
  val scaledImage = image.getScaledInstance(outWidth.toInt(), outHeight.toInt(), Image.SCALE_SMOOTH)
  val outImage = BufferedImage(size.width.toInt(), size.height.toInt(), BufferedImage.TYPE_INT_ARGB)
  outImage.graphics.drawImage(
    scaledImage,
    (size.width.toInt() / 2 - outWidth / 2).toInt(),
    (size.height.toInt() / 2 - outHeight / 2).toInt(),
    null
  )
  return outImage
}