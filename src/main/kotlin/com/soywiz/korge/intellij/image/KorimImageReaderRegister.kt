package com.soywiz.korge.intellij.image

import com.intellij.openapi.components.*
import com.intellij.openapi.project.*
import javax.imageio.spi.*

@Service
class KorimImageReaderRegister : DumbAware {
    init {
        initialize
    }
    companion object {
        val initialize = run {
            println("KorimImageReaderRegister.init")
            val defaultInstance: IIORegistry = IIORegistry.getDefaultInstance()
            defaultInstance.registerServiceProvider(KorimImageReaderSpi(), ImageReaderSpi::class.java)
        }
    }
}
