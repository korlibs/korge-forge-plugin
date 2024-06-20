package com.soywiz.korge.settings

import com.intellij.openapi.options.*
import com.intellij.openapi.ui.*
import com.intellij.ui.dsl.builder.*
import com.soywiz.korge.intellij.config.*
import org.jetbrains.annotations.*

// Used as reference:
//   com.intellij.ui.ExperimentalUIConfigurable
class KorgeSettingsPageConfigurableProvider : ConfigurableProvider() {

    override fun createConfigurable(): Configurable? {
        return object : BoundSearchableConfigurable(
            "KorGE",
            "com.soywiz.korge.settings.KorgeSettingsPageConfigurableProvider"
        ) {
            //val tempGlobalSettings = KorgeGlobalSettings().also {
            //    it.useLocalStore = korgeGlobalSettings.useLocalStore
            //}

            override fun createPanel(): DialogPanel {
                return panel {
                    row {
                        checkBox("Use Local KorGE Store")
                            .bindSelected({ korgeGlobalSettings.useLocalStore }, {
                                korgeGlobalSettings.useLocalStore = it
                                println("korgeGlobalSettings.useLocalStore = $it : ${korgeGlobalSettings.useLocalStore}")
                            })
                            .comment("Uses http://127.0.0.1:4000/ instead of https://store.korge.org/ for development purposes")
                    }
                    //row {
                    //    checkBox("KorGE Preview Split by Default")
                    //        .bindSelected({ korgeGlobalSettings.korgeSplitWindowByDefault }, {
                    //            korgeGlobalSettings.korgeSplitWindowByDefault = it
                    //            println("korgeGlobalSettings.korgeSplitWindowByDefault = $it : ${korgeGlobalSettings.korgeSplitWindowByDefault}")
                    //        })
                    //        .comment("Starts with the KorGE Preview opened by default")
                    //}
                }
            }

            override fun isModified(): Boolean = true

            override fun reset() {
            }

            override fun apply() {
                super.apply()
                //if (isModified) {
                    //korgeGlobalSettings.useLocalStore = korgeGlobalSettings.useLocalStore
                //}
            }

            @Nls(capitalization = Nls.Capitalization.Title)
            override fun getDisplayName(): String = "KorGE"
            override fun getId(): String = "org.korge.KorgeSettingsPageConfigurable"

            //override fun enableSearch(option: String?): Runnable? {
            //    println("KorgeSettingsPageConfigurableProvider.enableSearch: $option")
            //    return null
            //}
        }
    }
}
