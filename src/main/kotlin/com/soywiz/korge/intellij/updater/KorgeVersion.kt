package com.soywiz.korge.intellij.updater

import com.intellij.ide.plugins.*
import com.intellij.openapi.extensions.*
import com.soywiz.korge.intellij.util.*
import korlibs.crypto.*
import korlibs.io.dynamic.*
import kotlinx.coroutines.*
import java.io.*
import java.net.*
import java.util.*
import java.util.concurrent.*

val KORGE_FORGE_VERSION by lazy {
    val korgePluginId = PluginId.findId("com.soywiz.korge.korge-intellij-plugin")
    val korgePlugin = korgePluginId?.let { PluginManagerCore.getPlugin(it) }
    korgePlugin?.version ?: "Unknown"
}

private val korgeCacheData = ConcurrentHashMap<String, String>()
val korgeCacheDir: File by lazy { File(System.getProperty("user.home"), ".korge").apply { if (!this.isDirectory) mkdirs() } }
//val node_modules by lazy { project.file("node_modules") }

val korgeInstallUUID: String get() {
    return korgeCacheData.getOrPut("korgeInstallUUID") {
        val uuidFile = File(korgeCacheDir, "install-uuid")
        if (!uuidFile.exists()) {
            uuidFile.writeText(UUID.randomUUID().toString().replace('7', '1').replace('3', '9').replace('a', 'f'))
        }
        uuidFile.readText()
    }
}

suspend fun korgeForgeVersionJson(telemetry: Boolean = false): String {
    return withContext(Dispatchers.IO) {
        val DEFAULT_JSON = "{\"forge.plugin.version\": \"${KORGE_FORGE_VERSION}\", \"installer.version\": \"0.0.0\"}"
        korgeCacheData.getOrPut("korgeVersionJson") {
            val versionForgeJsonFile = File(korgeCacheDir, "version.forge.json")
            if (!versionForgeJsonFile.isFile && System.currentTimeMillis() - versionForgeJsonFile.lastModified() >= 24 * 3600 * 1000L) {
                val base = "https://version.korge.org/version.json?source=ide&type=forge"
                val basicProps: Map<String, String> = mapOf(
                    //"version" to BuildVersions.KORGE,
                    "ide.version" to KORGE_FORGE_VERSION,
                    "ci" to (System.getenv("CI") == "true").toString(),
                    "os.name" to System.getProperty("os.name"),
                    "os.arch" to System.getProperty("os.arch"),
                    "os.version" to System.getProperty("os.version"),
                )
                val telemetryProps = mapOf(
                    "install.uuid" to korgeInstallUUID
                )
                val props = when {
                    telemetry -> basicProps + telemetryProps
                    else -> basicProps
                }

                fun Map<String, String>.toQueryString(): String {
                    return this.map {
                        URLEncoder.encode(it.key, Charsets.UTF_8) + "=" + URLEncoder.encode(it.value, Charsets.UTF_8)
                    }.joinToString("&")
                }
                try {
                    downloadFile(
                        URL("$base&${props.toQueryString()}"),
                        versionForgeJsonFile,
                        connectionTimeout = 5_000,
                        readTimeout = 3_000,
                    )
                } catch (e: Throwable) {
                    e.stackTraceToString()
                    versionForgeJsonFile.writeText(DEFAULT_JSON)
                }
            }
            try {
                versionForgeJsonFile.readText()
            } catch (e: Throwable) {
                DEFAULT_JSON
            }
        }
    }
}

//data class KorgeForgeVersionInfo(
//    val version: String,
//    val installerUrl: String,
//    val installerSha256: String,
//)

suspend fun korgeCheckVersion(report: Boolean = true, telemetry: Boolean = false): String? {
    val versionJson = korlibs.io.serialization.json.Json.parse(korgeForgeVersionJson(telemetry = telemetry)).dyn
    val latestVersion = versionJson["forge.plugin.version"].toStringOrNull()
    return if (latestVersion != KORGE_FORGE_VERSION) latestVersion else null
}

suspend fun downloadFile(url: URL, localFile: File, connectionTimeout: Int = 15_000, readTimeout: Int = 15_000): File {
    withContext(Dispatchers.IO) {
        //logger.info("Downloading $url into $localFile ...")
        url.openConnection().also {
            it.connectTimeout = connectionTimeout
            it.readTimeout = readTimeout
        }.getInputStream().use { input ->
            localFile.ensureParents().writeBytes(input.readAllBytes())
            //FileOutputStream(localFile.ensureParents()).use { output -> input.copyTo(output) }
        }
    }
    return localFile
}

suspend fun downloadFile(url: URL, localFile: File, sha256: String, connectionTimeout: Int = 15_000, readTimeout: Int = 15_000) {
    val downloadedFile = downloadFile(url, File(localFile.parentFile, "${localFile.name}.tmp"), connectionTimeout = connectionTimeout, readTimeout = readTimeout)
    val expectedSha256 = sha256.lowercase()
    val downloadedSha256 = downloadedFile.readBytes().sha256().hexLower
    if (downloadedSha256 != expectedSha256) {
        error("Downloaded file $url expected sha256=$expectedSha256 but found $downloadedSha256")
    }
    downloadedFile.renameTo(localFile)
}
