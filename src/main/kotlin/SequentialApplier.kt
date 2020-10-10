import IntentionHandler.Companion.editor
import IntentionHandler.Companion.file
import IntentionHandler.Companion.project
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager


class SequentialApplier {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = mutableSetOf<Int>()
    private val document = editor!!.document // Is it okay to put it here?

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    private fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(project) { command()
            PsiDocumentManager.getInstance(project!!).commitDocument(document)
        }
    }

    fun start() {
        val actions = IntentionHandler.getIntentionsList(true)

        val oldText = document.text
        if (actions.isEmpty()) return

        for (action in actions) {
//            println(action)
//            println(oldText)


            val intentionName = IntentionHandler.getIntentionActionByName(action)
            runWriteCommandAndCommit{ intentionName.invoke(project!!, editor, file) }

            val event = IntentionEvent(action, oldText.hashCode(), document.text.hashCode())
            events.add(event)
            if (event.hash_start !in hashes) hashes.add(event.hash_start)
            if (event.hash_end in hashes) {
                println("hash ${event.hash_end} is in hashes, returning")
                continue
            } else {
                hashes.add(event.hash_end)
                start()
                runWriteCommandAndCommit { document.setText(oldText) } // Replace code entirely with previous one

            }
        }



    }
}