import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile


class PluginRunAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        IntentionHandler.editor = e.getData(PlatformDataKeys.EDITOR)
        IntentionHandler.project = e.getData(PlatformDataKeys.PROJECT)
        IntentionHandler.file = e.getData(LangDataKeys.PSI_FILE)
//        println(file)
//        val element : PsiElement? = e.getData(LangDataKeys.PSI_ELEMENT)
//        println(element)
//        val element2 : PsiElement? = file?.findElementAt(editor.caretModel.offset)
//        println(element2)

        PopUpForm().initialize()


    }


}