import com.intellij.codeInsight.intention.IntentionAction
import com.intellij.codeInsight.intention.impl.config.IntentionManagerImpl
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.LangDataKeys
import com.intellij.openapi.actionSystem.PlatformDataKeys
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiFile
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

class CurrentPositionHandler {
    companion object {
        val manager = IntentionManagerImpl()
    }
    val project: Project
    val editor: Editor
    val file: PsiFile
    private var actions: List<IntentionAction>
    init {
        val whiteList = File("${GlobalStorage.out_path}/intentionJsons/intentionsWhiteList.json")
        val blackList = File("${GlobalStorage.out_path}/intentionJsons/intentionsBlackList.json")
        actions = when {
            whiteList.exists() -> {
                val list: List<String> = Json.decodeFromString(whiteList.readText())
                manager.availableIntentions.filter { it.familyName in list } // TODO check difference with IntentionManagerImpl.getInstance()
            }
            blackList.exists() -> {
                val list: List<String> = Json.decodeFromString(blackList.readText())
                manager.availableIntentions.filter { it.familyName !in list }
            }
            else -> {
                manager.availableIntentions
            }
        }

    }

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
        var isAvailable: Boolean = false
        ApplicationManager.getApplication().runReadAction { isAvailable = intention.isAvailable(project, editor, file) }
        return isAvailable
    }

    fun getIntentionsList(onlyAvailable: Boolean): List<IntentionAction> {
        return if (onlyAvailable) actions.filter { checkIntention(it) }.toList() else actions.toList()
    }

}