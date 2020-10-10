import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import java.util.*

class IntentionHandler {
    // Caret is in only one place at the moment of plugin start
    companion object {
        var project: Project? = null
        var editor: Editor? = null
        var file: PsiFile? = null
        private var intentionsMap = HashMap<String, IntentionAction>() // Used for getting action from string (change to some existing method later?)

        fun getIntentionActionByName(name: String) : IntentionAction {
            return intentionsMap[name]!!
        }

        fun checkSelectedIntentionByName(selected: String) : Boolean {
            return checkIntention(getIntentionActionByName(selected))
        }

        private fun checkIntention(intention: IntentionAction) : Boolean {
            return intention.isAvailable(project!!, editor, file)
        }

        fun getIntentionsList(onlyAvailable: Boolean): List<String> {
            var actions = IntentionManagerImpl().availableIntentionActions
            if (onlyAvailable) {
                actions = actions.filter { checkIntention(it) }.toTypedArray()
            }
            actions.forEach { intentionsMap[it.familyName] = it }
            return actions.map { it.familyName }
        }
    }
}