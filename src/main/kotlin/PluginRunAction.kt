import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys


class PluginRunAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        IntentionHandler.editor = e.getData(PlatformDataKeys.EDITOR)
        IntentionHandler.project = e.getData(PlatformDataKeys.PROJECT)
        IntentionHandler.file = e.getData(LangDataKeys.PSI_FILE)
        IntentionHandler.out_path = System.getenv()["OUT_PATH"]
//        println(file)
//        val element : PsiElement? = e.getData(LangDataKeys.PSI_ELEMENT)
//        println(element)
//        val element2 : PsiElement? = file?.findElementAt(editor.caretModel.offset)
//        println(element2)

        PopUpForm().initialize()


    }


}