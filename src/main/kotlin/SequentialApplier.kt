import GlobalStorage.out_path
import com.intellij.codeInsight.intention.impl.config.IntentionActionWrapper
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import kotlinx.serialization.*
import java.io.File
import kotlinx.serialization.json.Json

data class CodeState(var code: String, var offset: Int)

class SequentialApplier(private val handler: CurrentPositionHandler) {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = mutableMapOf<Int, String>()
    private val document = handler.editor.document // Is it okay to put it here?
    private val docManager = PsiDocumentManager.getInstance(handler.project)
    private val caret = handler.editor.caretModel
    private var setOfSemanticsChangingIntentions: Set<String> = emptySet() // TODO Should it be moved outside of the class?
    private val startingOffset = caret.offset

    init {
        val file = File("$out_path/badIntentions.json")
        setOfSemanticsChangingIntentions = Json.decodeFromString(file.readText())
    }

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    private fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(handler.project) {
            command()
        }
        WriteCommandAction.runWriteCommandAction(handler.project) { // This WriteAction isn't really necessary?
            docManager.commitDocument(document) // There is difference with commitAllDocuments
        }
    }

    private fun getLinesAroundOffset(text: String, offset: Int, linesAround: Int = 5) : String{
        val onLine = text.take(offset + 1).lines().size
        val lines = text.lines()
        return lines.subList(maxOf(onLine - linesAround, 0), minOf(onLine + linesAround, lines.size)).joinToString(separator = "\n")
    }

    fun start(depth: Int = 0, max_depth: Int = 20): Boolean {
        if (depth > max_depth) return false
        val actions = handler.getIntentionsList(true)

        val oldState = CodeState(document.text, caret.offset)
        var shouldBeContinued = true

        for (intention in actions) {
            val actionName = intention.familyName
            if (intention is IntentionActionWrapper) {
                if ("semantics" in intention.delegate.toString()) { // It doesn't filter all the semantic problems
                    println("$actionName(${intention.delegate} changes semantics?")
                    continue
                }
            }
            // Attempt to throw away "bad" intentions
            // "Introduce local variable" sometimes is OK, sometimes breaks everything
            if (!intention.startInWriteAction()) { // What's wrong with local variable?
                println("Skipping ${intention.familyName}")
                continue
            }

            /*
            This also drops "Convert number" (opens pop-up),  "Replace with block comment" (otherwise
            it becomes endless loop of adding spaces in the end),
             "Cast expression" (endless loop with "Create Local Var from instanceof Usage"),
             "Break string on '\n'"(loop)
             */
            if (setOfSemanticsChangingIntentions.contains(actionName)) continue
            runWriteCommandAndCommit {
                intention.isAvailable(handler.project, handler.editor, handler.file)
                intention.invoke(handler.project, handler.editor, handler.file)
            }
            val newCode = document.text
            val event = IntentionEvent(actionName, oldState.code.hashCode(), newCode.hashCode())
            events.add(event)
            println(event)
            if (event.hash_start !in hashes.keys) {
                hashes[event.hash_start] = getLinesAroundOffset(oldState.code, startingOffset) // Store only small pieces of code
            }

            if (event.hash_end !in hashes.keys) {
                hashes[event.hash_end] = getLinesAroundOffset(newCode, startingOffset)
                shouldBeContinued = start(depth + 1, max_depth)
                if (!shouldBeContinued) break
            }
            runWriteCommandAndCommit {
                document.setText(oldState.code)
            } // Replace code with old one
            caret.moveToOffset(oldState.offset)
        }
        return shouldBeContinued

    }

    fun dumpHashMap(parentFilename: String, filename: String) { // Write our map to file
        val file = File("$out_path/maps/$parentFilename/$filename.txt")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        if (!file.exists())
            file.createNewFile()
        file.printWriter().use { out ->
            hashes.forEach {
                out.println("${it.key}, ${it.value}")
            }
        }
    }


}