package com.soywiz.korge.intellij.util

import korlibs.image.color.*
import java.awt.*
import javax.swing.plaf.*

//val controlRgba = MetalLookAndFeel.getCurrentTheme().control.rgba()

fun ColorUIResource.rgba(): RGBA {
    return RGBA(red, green, blue, alpha)
}

fun Color.rgba(): RGBA {
    return RGBA(red, green, blue, alpha)
}

fun Color.blendTo(that: Color, ratio: Float = 0.5f): Color {
    val c1 = this
    val c2 = that
    var ratio = ratio
    if (ratio > 1f) ratio = 1f
    else if (ratio < 0f) ratio = 0f
    val iRatio = 1.0f - ratio

    val i1 = c1.rgb
    val i2 = c2.rgb

    val a1 = (i1 shr 24 and 0xff)
    val r1 = ((i1 and 0xff0000) shr 16)
    val g1 = ((i1 and 0xff00) shr 8)
    val b1 = (i1 and 0xff)

    val a2 = (i2 shr 24 and 0xff)
    val r2 = ((i2 and 0xff0000) shr 16)
    val g2 = ((i2 and 0xff00) shr 8)
    val b2 = (i2 and 0xff)

    val a = ((a1 * iRatio) + (a2 * ratio)).toInt()
    val r = ((r1 * iRatio) + (r2 * ratio)).toInt()
    val g = ((g1 * iRatio) + (g2 * ratio)).toInt()
    val b = ((b1 * iRatio) + (b2 * ratio)).toInt()

    return Color(a shl 24 or (r shl 16) or (g shl 8) or b)
}
