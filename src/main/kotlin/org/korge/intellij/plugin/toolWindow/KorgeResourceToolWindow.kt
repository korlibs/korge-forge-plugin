package org.korge.intellij.plugin.toolWindow

import com.google.common.cache.CacheBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.BaseProjectDirectories.Companion.getBaseDirectories
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.readBytes
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.soywiz.korge.intellij.annotator.*
import com.soywiz.korge.intellij.util.*
import org.korge.intellij.plugin.util.*
import java.awt.Dimension
import java.util.concurrent.TimeUnit
import javax.imageio.ImageIO
import javax.swing.Icon
import javax.swing.ImageIcon


class KorgeResourceToolWindow : ToolWindowFactory {
  override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
    val rootDir: VirtualFile? = project.getBaseDirectories().firstOrNull()

    val baseDir = rootDir?.findChild("resources")
                  ?: rootDir?.findChild("src")?.findChild("commonMain")?.findChild("resources")
                  ?: rootDir

    val iconCache = CacheBuilder.newBuilder()
      .maximumSize(200)
      .expireAfterAccess(10, TimeUnit.MINUTES)
      .build<VirtualFile, Icon>()

    val MAX_WIDTH = JGridPreviewer.MAX_WIDTH
    val MAX_HEIGHT = JGridPreviewer.MAX_HEIGHT
    val MAX_SIZE = Size(MAX_WIDTH, MAX_HEIGHT)

    val UP_LEVEL = AllIcons.Nodes.UpLevel.renderCentered(MAX_WIDTH, MAX_HEIGHT)
    val FOLDER = AllIcons.Nodes.Folder.renderCentered(MAX_WIDTH, MAX_HEIGHT)
    val FILE = AllIcons.FileTypes.Text.renderCentered(MAX_WIDTH, MAX_HEIGHT)
    val UNKNOWN = AllIcons.FileTypes.Unknown.renderCentered(MAX_WIDTH, MAX_HEIGHT)

    //toolWindow.contentManager.addContent(ContentImpl(JLabel(""), "", false))
    val gridPreview = JGridPreviewer(emptyList<VirtualFile?>())
    gridPreview.imageProvider = {
      when {
        it == null -> UP_LEVEL
        //it.name == ".." -> AllIcons.Nodes.UpLevel
        it.isDirectory -> FOLDER
        else -> iconCache.get(it) {
          try {
            when (it.extension?.lowercase()) {
              "jpg", "png" -> {
                val image = ImageIO.read(it.readBytes().inputStream())
                ImageIcon(image.getScaledInstanceNew(MAX_SIZE, ScaleMode.SHOW_ALL))
              }
              "ttf", "otf" -> {
                val (font, preview) = getFontPreview(null, 64, it.readBytes(), project.colorsScheme.defaultForeground)
                ImageIcon(preview.getScaledInstanceNew(MAX_SIZE, ScaleMode.SHOW_ALL))
              }
              else -> FILE
            }
          } catch (e: Throwable) {
            e.printStackTrace()
            UNKNOWN
          }
        }
      }
    }
    gridPreview.itemSize = Dimension(120, 120)
    gridPreview.labelProvider = { index, it -> if (it == null) ".." else it?.name ?: "$index" }
    var currentFolder: VirtualFile? = baseDir
    fun setFolder(folder: VirtualFile?) {
      println("SETFOLDER: folder=$folder")
      currentFolder = folder
      gridPreview.items = buildList {
        folder?.parent
        if (folder?.parent != null && baseDir.isAncestorOf(folder.parent)) add(null)
        addAll((folder?.children?.toList() ?: emptyList()))
      }
    }
    setFolder(baseDir)
    gridPreview.onOpen.add {
      println("ONOPEN: it=$it")
      when {
        it == null -> setFolder(currentFolder?.parent)
        it.isDirectory -> setFolder(it)
        else -> {
            FileEditorManager.getInstance(project).openFile(it, false)
            ActionManager.getInstance().getAction("RevealIn")?.actionPerformed(AnActionEvent.createFromDataContext("place", null, project.dataContext))
        }
      }
    }
    val content: Content = ContentFactory.getInstance().createContent(gridPreview, "", false)
    toolWindow.contentManager.addContent(content)
    toolWindow.setTitleActions(listOf(
        object : AnAction("Refresh", "Refresh files", AllIcons.Actions.Refresh) {
          override fun actionPerformed(e: AnActionEvent) {
            iconCache.invalidateAll()
            setFolder(currentFolder)
          }
        },
        object : AnAction("Root", "Go Root", AllIcons.Actions.MoveUp) {
            override fun actionPerformed(e: AnActionEvent) {
                iconCache.invalidateAll()
                setFolder(baseDir)
            }
        }
    ))

    toolWindow.show()
  }
}
