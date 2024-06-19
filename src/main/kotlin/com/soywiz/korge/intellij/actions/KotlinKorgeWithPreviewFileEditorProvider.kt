package com.soywiz.korge.intellij.actions;

import com.intellij.openapi.editor.*
import com.intellij.openapi.fileEditor.*
import com.intellij.openapi.fileEditor.impl.text.*
import com.intellij.openapi.project.*
import com.intellij.openapi.util.*
import com.intellij.openapi.vfs.*
import org.jetbrains.kotlin.idea.util.*
import org.korge.intellij.plugin.toolWindow.*

private const val KOTLIN_KORGE_PREVIEW_EDITOR_PROVIDER: String = "KotlinKorgeWithPreviewFileEditorProvider"

// REFERENCE: KtScratchFileEditorProvider

public class KotlinKorgeWithPreviewFileEditorProvider : FileEditorProvider, DumbAware {
    override fun getEditorTypeId(): String = KOTLIN_KORGE_PREVIEW_EDITOR_PROVIDER

    override fun accept(project: com.intellij.openapi.project.Project, file: VirtualFile): Boolean {
        if (!file.isValid) {
            return false
        }
        //if (!file.isKotlinFileType() && !file.isKotlinWorksheet) {
        //    return false
        //}
        //val psiFile = ApplicationManager.getApplication().runReadAction(Computable { PsiManager.getInstance(project).findFile(file) })
        //    ?: return false
        //return ScratchFileLanguageProvider.get(psiFile.fileType) != null
        return file.isKotlinFileType()
    }

    override fun acceptRequiresReadAction(): Boolean = false
    override fun createEditor(project: com.intellij.openapi.project.Project, file: VirtualFile): FileEditor {
        //val scratchFile = createScratchFile(project, file) ?: return TextEditorProvider.getInstance().createEditor(project, file)
        //return KtScratchFileEditorWithPreview.create(scratchFile)

        return KotlinKorgeFileEditorWithPreview.create(project, file)
    }

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}

class KotlinKorgeFileEditorWithPreview private constructor(
    val vfile: VirtualFile,
    sourceTextEditor: TextEditor,
    previewEditor: FileEditor
) : TextEditorWithPreview(sourceTextEditor, previewEditor), TextEditor {
    companion object {
        fun create(project: com.intellij.openapi.project.Project, file: VirtualFile): KotlinKorgeFileEditorWithPreview {
            val textEditorProvider = TextEditorProvider.getInstance()

            val mainEditor = textEditorProvider.createEditor(project, file) as TextEditor
            val editorFactory = EditorFactory.getInstance()

            val viewer = editorFactory.createViewer(editorFactory.createDocument(""), project, EditorKind.PREVIEW)
            Disposer.register(mainEditor) { editorFactory.releaseEditor(viewer) }

            //val previewEditor = textEditorProvider.getTextEditor(viewer)
            val previewEditor = GamePreviewFileEditor(project)

            return KotlinKorgeFileEditorWithPreview(file, mainEditor, previewEditor)
        }
    }
}
