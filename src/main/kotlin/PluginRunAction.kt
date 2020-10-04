import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile


class PluginRunAction : AnAction() {
    // Caret is in only one place at the moment of plugin start
    companion object {
        var project: Project? = null
        var editor: Editor? = null
        var file: PsiFile? = null

        fun checkSelectedIntention(selected: IntentionAction) : Boolean {
            return selected.isAvailable(project!!, editor, file)
        }
    }
    override fun actionPerformed(e: AnActionEvent) {
        editor = e.getData(PlatformDataKeys.EDITOR)
        project = e.getData(PlatformDataKeys.PROJECT)
//        println(editor)
//        println(project)
        file = e.getData(LangDataKeys.PSI_FILE)
//        println(file)
//        val element : PsiElement? = e.getData(LangDataKeys.PSI_ELEMENT)
//        println(element)
//        val element2 : PsiElement? = file?.findElementAt(editor.caretModel.offset)
//        println(element2)

        PopUpForm()


    }


}