package com.soywiz.korge.intellij.util

import com.intellij.openapi.application.*
import korlibs.encoding.*
import korlibs.io.file.std.*
import java.io.*
import java.net.*
import java.security.*

fun File.ensureParents(): File = this.also { it.parentFile.mkdirs() }

fun getUrlCachedFile(url: String): File {
    val urlHash = MessageDigest.getInstance("SHA256").digest(url.toByteArray(Charsets.UTF_8)).hexLower
    return File(PathManager.getPluginTempPath())["downloadCache"]["v4-$urlHash.bin"]
}

fun getUrlCached(url: String): ByteArray? {
    val cacheFile = getUrlCachedFile(url)
    return cacheFile.takeIf { it.exists() }?.readBytes()
}

fun deleteCachedUrl(url: String) {
    getUrlCachedFile(url).delete()
}

fun downloadUrlCached(url: String, cacheForMillis: Long = 24 * 3600 * 1000): ByteArray {
    val cacheFile = getUrlCachedFile(url)
    if (!cacheFile.exists() || (System.currentTimeMillis() - cacheFile.lastModified()) >= cacheForMillis) {
        //runBackgroundableTask("Downloading $url") {
        cacheFile.ensureParents().writeBytes(URL(url).readBytes())
        //}
    }
    return cacheFile.readBytes()
}
