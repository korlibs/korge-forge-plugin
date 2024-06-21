package org.korge.intellij.plugin.ipcpreview

import korlibs.korge.ipc.*

data class KorgeIPCInfo(val path: String = System.getenv("KORGE_IPC") ?: DEFAULT_PATH) {
    companion object {
        val DEFAULT_PATH get() = KorgeIPC.DEFAULT_PATH
    }
}

fun KorgeIPCInfo.createIPC(): KorgeIPC = KorgeIPC(path)

fun KorgeIPC.close() {
    println("/KorgeIPC:$path")
    frame.close()
    events.close()
}
