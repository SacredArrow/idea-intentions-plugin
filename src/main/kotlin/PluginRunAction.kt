import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent


class PluginRunAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        GlobalStorage.out_path = System.getenv()["OUT_PATH"]
//        println(file)
//        val element : PsiElement? = e.getData(LangDataKeys.PSI_ELEMENT)
//        println(element)
//        val element2 : PsiElement? = file?.findElementAt(editor.caretModel.offset)
//        println(element2)

        PopUpForm().initialize(e)


    }


}