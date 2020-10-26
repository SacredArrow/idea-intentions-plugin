import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile

class CurrentPositionHandler {
    val project: Project
    val editor: Editor
    val file: PsiFile
    var actions: Array<IntentionAction> = IntentionManagerImpl().availableIntentionActions // TODO check difference with IntentionManagerImpl.getInstance()

    constructor(project: Project, editor: Editor, file: PsiFile) {
        this.project = project
        this.editor = editor
        this.file = file
    }
    constructor(e: AnActionEvent)  {
        val project = e.getData(PlatformDataKeys.PROJECT)!!
        val editor = e.getData(PlatformDataKeys.EDITOR)!! // If cursor isn't placed, editor and file will be null
        val file = e.getData(LangDataKeys.PSI_FILE)!!
        this.project = project
        this.editor = editor
        this.file = file
    }

    private fun checkIntention(intention: IntentionAction) : Boolean {
        return intention.isAvailable(project, editor, file)
    }

    fun getIntentionsList(onlyAvailable: Boolean): List<IntentionAction> {
        return if (onlyAvailable) actions.filter { checkIntention(it) }.toList() else actions.toList()
    }

}