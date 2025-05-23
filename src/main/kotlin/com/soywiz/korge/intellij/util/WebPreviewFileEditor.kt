package com.soywiz.korge.intellij.util

import com.intellij.*
import com.intellij.ide.*
import com.intellij.ide.browsers.*
import com.intellij.ide.browsers.actions.*
import com.intellij.openapi.application.*
import com.intellij.openapi.diagnostic.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.options.*
import com.intellij.openapi.project.*
import com.intellij.openapi.ui.popup.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import com.intellij.ui.*
import com.intellij.ui.jcef.*
import org.jetbrains.annotations.ApiStatus.*
import org.jetbrains.ide.*
import java.awt.*
import java.beans.*
import java.util.concurrent.atomic.*
import javax.swing.*

private const val WEB_PREVIEW_RELOAD_TOOLTIP_ID: String = "web.preview.reload.on.save"

@Internal
class MyWebPreviewFileEditor internal constructor(file: WebPreviewVirtualFile) : UserDataHolderBase(), FileEditor {
    private val file = file.originalFile
    private val panel: JCEFHtmlPanel
    private val url = file.previewUrl.toExternalForm()

    @Deprecated("Use {@link #WebPreviewFileEditor(WebPreviewVirtualFile)}", level = DeprecationLevel.ERROR)
    constructor(project: Project, file: WebPreviewVirtualFile) : this(file)

    init {
        panel = object : JCEFHtmlPanel(url) {
            override fun getBackgroundColor(): Color = Color.WHITE
        }
    }

    companion object {
        private val previewsOpened = AtomicInteger()

        val isPreviewOpened: Boolean
            get() = previewsOpened.get() > 0
    }

    internal fun reloadPage() {
        panel.loadURL(url)
        previewsOpened.incrementAndGet()
        showPreviewTooltip()
    }

    private fun showPreviewTooltip() {
        ApplicationManager.getApplication().invokeLater {
            val gotItTooltip = GotItTooltip(
                WEB_PREVIEW_RELOAD_TOOLTIP_ID, BuiltInServerBundle.message("reload.on.save.preview.got.it.content"),
                this)
            if (!gotItTooltip.canShow()) {
                return@invokeLater
            }

            if (WebBrowserManager.PREVIEW_RELOAD_MODE_DEFAULT != ReloadMode.RELOAD_ON_SAVE) {
                logger<WebPreviewFileEditor>().error(
                    "Default value for " + BuiltInServerBundle.message("reload.on.save.preview.got.it.title") + " has changed, tooltip is outdated.")
                return@invokeLater
            }
            if (WebBrowserManager.getInstance().webPreviewReloadMode != ReloadMode.RELOAD_ON_SAVE) {
                // changed before gotIt was shown
                return@invokeLater
            }

            gotItTooltip
                .withHeader(BuiltInServerBundle.message("reload.on.save.preview.got.it.title"))
                .withPosition(Balloon.Position.above)
                .withLink(CommonBundle.message("action.text.configure.ellipsis"), Runnable {
                    ShowSettingsUtil.getInstance().showSettingsDialog(null, { it: Configurable? ->
                        it is SearchableConfigurable &&
                            it.id == "reference.settings.ide.settings.web.browsers"
                    }, null)
                })
            gotItTooltip.show(panel.component) { _, _ -> Point(0, 0) }
        }
    }

    override fun getComponent(): JComponent = panel.component

    override fun getPreferredFocusedComponent() = panel.component

    override fun getName(): String = IdeBundle.message("web.preview.file.editor.name", file.name)

    override fun setState(state: FileEditorState) {
    }

    override fun getFile(): VirtualFile = file

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun removePropertyChangeListener(listener: PropertyChangeListener) {
    }

    override fun dispose() {
        previewsOpened.decrementAndGet()
        Disposer.dispose(panel)
    }
}
