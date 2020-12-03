import GlobalStorage.out_path
import com.intellij.codeInsight.intention.impl.config.IntentionActionWrapper
import com.intellij.ide.IdeEventQueue
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.psi.PsiDocumentManager
import kotlinx.serialization.*
import java.io.File
import kotlinx.serialization.json.Json

data class CodeState(val code: String, val offset: Int)

@Serializable
data class CodePiece(val hash: Int, val code: String, val intentionLine: Int)

class SequentialApplier(private val handler: CurrentPositionHandler) {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = mutableMapOf<Int, Pair<Int, String>>()
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

    private fun getLinesAroundOffset(text: String, offset: Int) : Pair<Int, String>{
        val onLine = text.take(offset + 1).lines().size
        val lines = text.lines()
        val code = lines.subList(maxOf(onLine - GlobalStorage.linesAround, 0), minOf(onLine + GlobalStorage.linesAround, lines.size)).joinToString(separator = "\n")
        return Pair(minOf(onLine + 1, GlobalStorage.linesAround), code)
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
                    println("$actionName(${intention.delegate} changes semantics? Skipping...")
                    continue
                }
            }
            // Attempt to throw away "bad" intentions
            // "Introduce local variable" sometimes is OK, sometimes breaks everything
            if (!intention.startInWriteAction()) {
                println("Skipping ${intention.familyName}")
                continue
            }

            // This is useful only when editor is displayed. Otherwise it can't see that popup is active (and it isn't called before it)
            if (IdeEventQueue.getInstance().isPopupActive) {
                println("Skipping ${intention.familyName} because it needs popup")
                IdeEventQueue.getInstance().popupManager.closeAllPopups()
                continue
            }

            /*
            This also drops "Replace with block comment" (otherwise
            it becomes endless loop of adding spaces in the end),
             "Cast expression" (endless loop with "Create Local Var from instanceof Usage"),
             "Break string on '\n'"(loop)
             */
            if (setOfSemanticsChangingIntentions.contains(actionName)) {
                println("Skipping ${intention.familyName} because it is in the list")
                continue
            }
//            println(actionName)
            try {
                runWriteCommandAndCommit {
                    intention.isAvailable(handler.project, handler.editor, handler.file)
                    intention.invoke(handler.project, handler.editor, handler.file)
                }
            } catch (e: AssertionError) {
                if (e.message == "Editor must be showing on the screen") {
                    println("Editor assertion skipped")
                    continue
                } else {
                    throw(e)
                }
            }
            val newCode = document.text
            val event = IntentionEvent(actionName, oldState.code.hashCode(), newCode.hashCode())
            events.add(event)
            GlobalStorage.usedIntentions.add(actionName)
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
        val file = File("$out_path/maps/$parentFilename/$filename.json")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        if (!file.exists())
            file.createNewFile()
        val codePieces = mutableListOf<CodePiece>()
        hashes.forEach {
            codePieces.add(CodePiece(it.key, it.value.second, it.value.first))
        }

        file.writeText(Json{prettyPrint = true}.encodeToString(codePieces))
    }


}