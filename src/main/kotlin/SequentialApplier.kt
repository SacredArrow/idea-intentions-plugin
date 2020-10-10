import IntentionHandler.Companion.editor
import IntentionHandler.Companion.file
import IntentionHandler.Companion.project
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.impl.PsiDocumentManagerImpl


class SequentialApplier {
    var events = mutableListOf<IntentionEvent>()
    var hashes = mutableSetOf<Int>()

    fun start() {
        val actions = IntentionHandler.getIntentionsList(true)
        val document = editor!!.document
        val oldText = document.text
        if (actions.isEmpty()) return

        for (action in actions) {
            println(action)
            println(oldText)

            // Explanation of the following command: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html#invokelater
            val intentionName = IntentionHandler.getIntentionActionByName(action)

            WriteCommandAction.runWriteCommandAction(project) { intentionName.invoke(project!!, editor, file)
                PsiDocumentManager.getInstance(project!!).commitDocument(document)}

            val event = IntentionEvent(action, oldText.hashCode(), document.text.hashCode())
            events.add(event)
            if (event.hash_start !in hashes) hashes.add(event.hash_start)
            if (event.hash_end in hashes) {
                println("hash ${event.hash_end} is in hashes, returning")
                return // Test purposes, TODO change to continue
            } else {
                hashes.add(event.hash_end)
                start()
                WriteCommandAction.runWriteCommandAction(project) {
//                    document.replaceString(0, document.textLength, oldText)
                    document.setText(oldText)
                    PsiDocumentManager.getInstance(project!!).commitDocument(document)
                } // Replace code entirely with previous one

            }
        }



    }
}