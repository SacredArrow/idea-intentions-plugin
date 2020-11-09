import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

// This class checks if all intentions from "referenceExamples" folder can be applied
class IntentionsAsserter(handler: CurrentPositionHandler) {
    private val project = handler.project
    fun start() {
        val map = Json.decodeFromString<Map<String, Int>>(File("${GlobalStorage.out_path}/referenceExamples/spots.json").readText())
        var trueAssertions = 0
        var falseAssertions = 0
        for ((key, value) in map) {
            println(key)
            val path = "${GlobalStorage.out_path}/referenceExamples/$key.java"
//            println(path)
            val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path")!!
            val file = PsiManager.getInstance(project).findFile(virtualFile)!!
            val document = PsiDocumentManager.getInstance(project).getDocument(file)
            var editor: Editor? = null
            WriteCommandAction.runWriteCommandAction(project) {
                editor = EditorFactory.getInstance().createEditor(document!!) // Must be invoked in EDT?
            }
            val handler = CurrentPositionHandler(project, editor!!, file)
            handler.editor.caretModel.moveToOffset(value)
            if (key in handler.getIntentionsList(true).map{it.familyName}) {
                trueAssertions++
                println("$key: TRUE")
            } else {
                falseAssertions++
                println("$key: FALSE")
            }
        }
        println("$trueAssertions TRUE assertions")
        println("$falseAssertions FALSE assertions")
    }
}