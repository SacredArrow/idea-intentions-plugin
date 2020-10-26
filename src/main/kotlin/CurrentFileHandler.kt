import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.util.*

class CurrentFileHandler {
    val project: Project
    val editor: Editor
    val file: PsiFile

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

    private var intentionsMap = HashMap<String, IntentionAction>() // Used for getting action from string (change to some existing method later?)

    fun getIntentionActionByName(name: String) : IntentionAction {
        return intentionsMap[name]!!
    }

    fun checkSelectedIntentionByName(selected: String) : Boolean {
        return checkIntention(getIntentionActionByName(selected))
    }

    private fun checkIntention(intention: IntentionAction) : Boolean {
        return intention.isAvailable(project, editor, file)
    }

    fun getIntentionsList(onlyAvailable: Boolean): List<String> {
        var actions = IntentionManagerImpl().availableIntentionActions // TODO check difference with IntentionManagerImpl.getInstance()
        if (onlyAvailable) {
            actions = actions.filter { checkIntention(it) }.toTypedArray()
        }
        actions.forEach { intentionsMap[it.familyName] = it }
        return actions.map { it.familyName } // TODO check if this should be changed to getText()
    }

}