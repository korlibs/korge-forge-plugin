package com.soywiz.korge.intellij.annotator

import com.intellij.lang.annotation.*
import com.intellij.psi.*
import org.jetbrains.yaml.psi.*

class DepsKProjectYmlAnnotator : Annotator {
    companion object {
        fun isDependenciesBlock(element: PsiElement): Boolean {
            if (element !is YAMLKeyValue) return false
            return element.keyText == "dependencies"
        }
    }

    override fun annotate(element: PsiElement, holder: AnnotationHolder) {
        if (!isDependenciesBlock(element)) return
        KorgeStoreAnnotation.annotate(element, holder)
    }
}
