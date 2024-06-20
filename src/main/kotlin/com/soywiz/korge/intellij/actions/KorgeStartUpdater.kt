package com.soywiz.korge.intellij.actions

import com.intellij.icons.AllIcons
import com.intellij.notification.*
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.*
import com.intellij.openapi.project.*
import com.soywiz.korge.intellij.config.*
import com.soywiz.korge.intellij.updater.*
import com.soywiz.korge.intellij.util.*
import korlibs.platform.*
import korlibs.time.*
import kotlinx.coroutines.*
import java.io.*
import java.net.*
import java.nio.file.attribute.PosixFilePermission
import kotlin.io.path.*

open class KorgeStartUpdater : KorgeAction("Start KorGE Forge Updater", icon = AllIcons.Ide.Notification.IdeUpdate, updateThread = ActionUpdateThread.BGT) {
    override fun actionPerformed(e: AnActionEvent) {
        execute(e.project ?: return)
    }
    companion object {
        fun execute(project: Project) {
            println("KorgeStartUpdater[1]")
            val localFileInstaller = File(korgeCacheDir, if (Platform.isWindows) "install-korge-forge.cmd" else "install-korge-forge.sh")
            localFileInstaller.delete()
            when {
                Platform.isWindows -> {
                    runBlocking { downloadFile(URL("https://forge.korge.org/install-korge-forge.cmd"), localFileInstaller) }
                    println("KorgeStartUpdater[2]")
                    ProcessBuilder("cmd", "/c", "start", "cmd", "/c", localFileInstaller.absolutePath).directory(localFileInstaller.parentFile).start()
                }
                else -> {
                    runBlocking { downloadFile(URL("https://forge.korge.org/install-korge-forge.sh"), localFileInstaller) }
                    localFileInstaller.toPath().setPosixFilePermissions(setOf(PosixFilePermission.OWNER_EXECUTE, PosixFilePermission.GROUP_EXECUTE, PosixFilePermission.OTHERS_EXECUTE))
                    println("KorgeStartUpdater[2]")
                    ProcessBuilder("cmd", "/c", "start", "cmd", "/c", localFileInstaller.absolutePath).directory(localFileInstaller.parentFile).start()
                }
            }
            println("KorgeStartUpdater[3]")
            println("UPDATE KORGE FORGE")
            ApplicationManager.getApplication().exit(true, true, false, 0)
        }
    }
}

open class KorgeCheckForUpdates : KorgeAction("KorGE Forge Check for Updates", updateThread = ActionUpdateThread.BGT) {
    override fun actionPerformed(e: AnActionEvent) {
        execute(e.project ?: return)
    }

    companion object {
        fun execute(project: Project) {
            try {
                CoroutineScope(Dispatchers.IO).launch {
                    val korgeNewVersion = korgeCheckVersion()

                    if (korgeNewVersion != null) {
                        val notification = Notification("korge-update", "New KorGE Forge $korgeNewVersion version available!", NotificationType.IDE_UPDATE)
                        notification.addAction(KorgeStartUpdater())
                        //notification.addAction(NotificationAction.createSimpleExpiring("Skip") {})
                        //notification.setSuggestionType(true)
                        notification.setImportantSuggestion(true)

                        notification.notify(project)
                    }
                }

            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
    }
}

open class KorgeCheckForUpdatesOnceADay : KorgeAction("KorGE Forge Check for Updates Once A Day") {
    override fun actionPerformed(e: AnActionEvent) {
        execute(e.project ?: return)
    }

    companion object {
        fun execute(project: Project) {
            val settings = korgeGlobalSettings
            val now = System.currentTimeMillis()
            val elapsedTimeSinceLastCheck = (now - settings.forgeUpdateLastChecked).milliseconds
            println("KorgeCheckForUpdatesOnceADay: elapsedTimeSinceLastCheck=$elapsedTimeSinceLastCheck, lastChecked=${settings.forgeUpdateLastChecked}")
            if (elapsedTimeSinceLastCheck >= 1.days) {
                settings.forgeUpdateLastChecked = now
                KorgeCheckForUpdates.execute(project)
            }
        }
    }
}
