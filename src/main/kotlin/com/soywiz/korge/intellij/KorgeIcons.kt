package com.soywiz.korge.intellij

import com.intellij.icons.*
import com.intellij.openapi.util.*
import com.intellij.ui.*
import javax.swing.*

// https://jetbrains.design/intellij/resources/icons_list/
object KorgeIcons {
    @JvmField val KTREE: Icon = getIcon("/com/soywiz/korge/intellij/icon/korge.png")
    @JvmField val JITTO: Icon = getIcon("/com/soywiz/korge/intellij/icon/jitto.png")
	@JvmField val PARTICLE: Icon = getIcon("/com/soywiz/korge/intellij/icon/particle.png")
	@JvmField val BITMAP_FONT: Icon = getIcon("/com/soywiz/korge/intellij/icon/bitmap_font.png")
	@JvmField val KRITA: Icon = getIcon("/com/soywiz/korge/intellij/icon/krita.png")
    @JvmField val QOI: Icon = getIcon("/com/soywiz/korge/intellij/icon/qoi.png")
	@JvmField val SWF: Icon = getIcon("/com/soywiz/korge/intellij/icon/swf.png")
	@JvmField val TILED: Icon = getIcon("/com/soywiz/korge/intellij/icon/tiled.png")
	@JvmField val KORGE: Icon = getIcon("/com/soywiz/korge/intellij/icon/korge.png")
	@JvmField val SPONSOR: Icon = getIcon("/com/soywiz/korge/intellij/icon/sponsor.png")
	@JvmField val VOICE: Icon = getIcon("/com/soywiz/korge/intellij/icon/lips.png")
	@JvmField val SPRITER: Icon = getIcon("/com/soywiz/korge/intellij/icon/spriter.png")
	@JvmField val SOUND: Icon = getIcon("/com/soywiz/korge/intellij/icon/sound.png")
	@JvmField val ATLAS: Icon = getIcon("/com/soywiz/korge/intellij/icon/atlas.png")
    @JvmField val SPINE: Icon = getIcon("/com/soywiz/korge/intellij/icon/spine.png")
    @JvmField val DRAGONBONES: Icon = getIcon("/com/soywiz/korge/intellij/icon/dragonbones.png")

    object ToolWindow {
        @JvmField val STORE: Icon = load("com/soywiz/korge/intellij/icon/toolwindows/toolWindowStore.svg", -100002, 2)
    }

	val USER_UNKNOWN_BYTES by lazy { getResourceBytes("/com/soywiz/korge/intellij/image/user_unknown.png") }

    fun getResourceBytes(path: String): ByteArray? =
        try {
            KorgeIcons::class.java.classLoader.getResourceAsStream(normalizePath(path))?.readBytes()
        } catch (e: Throwable) {
            e.printStackTrace()
            null
        }

    fun getIcon(path: String): Icon = try {IconLoader.getIcon(normalizePath(path), KorgeIcons::class.java)
    } catch (e: Throwable) {
        e.printStackTrace()
        AllIcons.Ide.ErrorPoint
    }

    private fun normalizePath(path: String): String = path.trimStart('/')

    private fun load(path: String, cacheKey: Int, flags: Int): Icon {
        return IconManager.getInstance().loadRasterizedIcon(path, KorgeIcons::class.java.classLoader, cacheKey, flags)
    }
}
