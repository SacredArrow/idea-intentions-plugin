import IntentionHandler.Companion.editor
import IntentionHandler.Companion.file
import IntentionHandler.Companion.project
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import java.io.File


class SequentialApplier {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = HashMap<Int, String>()
    private val document = editor!!.document // Is it okay to put it here?
    private val docManager = PsiDocumentManager.getInstance(project!!)

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    private fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(project) {
            command()
            docManager.commitDocument(document)
        }
    }

    fun start() {
        val actions = IntentionHandler.getIntentionsList(true)

        val oldCode = document.text

        for (action in actions) {
            val intention = IntentionHandler.getIntentionActionByName(action)
            runWriteCommandAndCommit { intention.invoke(project!!, editor, file) }
            val newCode = document.text
            val event = IntentionEvent(action, oldCode.hashCode(), newCode.hashCode())
            events.add(event)
            if (event.hash_start !in hashes.keys) hashes[event.hash_start] = oldCode
            if (event.hash_end !in hashes) {
                hashes[event.hash_end] = newCode
                start()
            }
            runWriteCommandAndCommit { document.setText(oldCode) } // Rplace code with old one
        }
    }

    fun dumpHashMap() { // Write our map to file
        File("/home/custos/Projects/Diploma/map.txt").printWriter().use { out ->
            hashes.forEach {
                out.println("${it.key}, ${it.value}")
            }
        }
    }




}