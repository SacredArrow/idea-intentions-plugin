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

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    private fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(project) { command()
            PsiDocumentManager.getInstance(project!!).commitDocument(document)
        }
    }

    fun start() {
        val actions = IntentionHandler.getIntentionsList(true)

        val oldCode = document.text
        if (actions.isEmpty()) return

        for (action in actions) {
            val intentionName = IntentionHandler.getIntentionActionByName(action)
            runWriteCommandAndCommit { intentionName.invoke(project!!, editor, file) }
            val newCode = document.text
            val event = IntentionEvent(action, oldCode.hashCode(), newCode.hashCode())
            events.add(event)
            if (event.hash_start !in hashes.keys) hashes[event.hash_start] = oldCode
            if (event.hash_end in hashes) {
                continue
            } else {
                hashes[event.hash_end] = newCode
                start()
                runWriteCommandAndCommit { document.setText(oldCode) } // Replace code entirely with previous one

            }
        }
    }

    fun dumpHashMap() {
        File("/home/custos/Projects/Diploma/map.txt").printWriter().use { out ->
            hashes.forEach {
                out.println("${it.key}, ${it.value}")
            }
        }
    }




}