import IntentionHandler.Companion.editor
import IntentionHandler.Companion.file
import IntentionHandler.Companion.project
import com.intellij.codeInsight.intention.impl.config.IntentionActionWrapper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import java.io.File

data class CodeState(var code: String, var offset: Int)

class SequentialApplier {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = HashMap<Int, String>()
    private val document = editor!!.document // Is it okay to put it here?
    private val docManager = PsiDocumentManager.getInstance(project!!)
    private val caret = editor!!.caretModel

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    private fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(project) {
            command()
            docManager.commitDocument(document) // There is difference with commitAllDocuments
        }
    }

    fun start() {
        val actions = IntentionHandler.getIntentionsList(true)

        val oldState = CodeState(document.text, caret.offset)

        for (actionName in actions) {
            val intention = IntentionHandler.getIntentionActionByName(actionName)
            if (actionName == "Change access modifier" || actionName == "Implement abstract class or interface") {
                continue
            }
            if (intention is IntentionActionWrapper) { // Temporary solution
                if (intention.delegate::class.qualifiedName!! in intention.delegate.toString()) {
                    println("$actionName is suspicious, skipping")
                    continue
                }
            } else {
                println("Encountered intention not from IntentionActionWrapper")
                continue
            }
            if (document.text != oldState.code) {
                println("Code doesn't match")
                println(document.text)
                println(oldState.code)
                return
            }
//            println(intention.isAvailable(project!!, editor, file))
            runWriteCommandAndCommit { intention.invoke(project!!, editor, file) }
            val newCode = document.text
            val event = IntentionEvent(actionName, oldState.code.hashCode(), newCode.hashCode())
            events.add(event)
            println(event)
            if (event.hash_start !in hashes.keys) hashes[event.hash_start] = oldState.code
            if (event.hash_end !in hashes.keys) {
                hashes[event.hash_end] = newCode
                start()
            }
            runWriteCommandAndCommit {
                document.setText(oldState.code)
            } // Replace code with old one
            caret.moveToOffset(oldState.offset)
        }
    }

    fun dumpHashMap(filename: String) { // Write our map to file
        val file = File("${IntentionHandler.out_path}/maps/$filename.txt")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs();
        if (!file.exists())
            file.createNewFile();
        file.printWriter().use { out ->
            hashes.forEach {
                out.println("${it.key}, ${it.value}")
            }
        }
    }




}