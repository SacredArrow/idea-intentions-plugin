import GlobalStorage.out_path
import com.intellij.codeInsight.intention.impl.config.IntentionActionWrapper
import com.intellij.ide.IdeEventQueue
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.lang.java.JavaLanguage
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.CaretModel
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.IdeFocusManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFileFactory
import com.intellij.psi.impl.PsiFileFactoryImpl
import kotlinx.serialization.*
import java.io.File
import kotlinx.serialization.json.Json

data class CodeState(val code: String, val offset: Int)

// Hash of -1 is used inside map
// Empty path is used inside map

@Serializable
data class CodePiece(
    var hash: Int,
    val code: String,
    val intentionLine: Int,
    val codePieceStart: Int,
    val codePieceEnd: Int,
    val offset: Int,
    var path: String,
    val fullCode: String
)

class SequentialApplier(private val handler: CurrentPositionHandler) {
    var events = mutableListOf<IntentionEvent>()
    private var hashes = mutableMapOf<Int, CodePiece>()
    private val document = handler.editor.document // Is it okay to put it here?
    private val docManager = PsiDocumentManager.getInstance(handler.project)
    private val caret = handler.editor.caretModel
    private var setOfSemanticsChangingIntentions: Set<String> = emptySet() // TODO Should it be moved outside of the class?
    private val startingOffset = caret.offset

    init {
        val file = this::class.java.classLoader.getResource("badIntentions.json")
        setOfSemanticsChangingIntentions = Json.decodeFromString(file.readText())
//        val psiFile = PsiFileFactory.getInstance(handler.project).createFileFromText(JavaLanguage.INSTANCE, handler.file.text) // Make dumb file so the original is not changed
//        this.document = docManager.getDocument(psiFile)!!
//        this.document = handler.editor.document
////        val editor = EditorFactory.getInstance().createEditor(this.document, handler.project, JavaFileType.INSTANCE, false) // Must be invoked in EDT?
//        val editor = handler.editor
//        this.caret = editor!!.caretModel
//        this.caret.moveToOffset(handler.editor.caretModel.offset)
////        this.handler = CurrentPositionHandler(handler.project, editor, psiFile)
//        this.handler = handler
//        this.startingOffset = caret.offset

    }

    // Explanation of the following function: https://jetbrains.org/intellij/sdk/docs/basics/architectural_overview/general_threading_rules.html
    fun runWriteCommandAndCommit(command: () -> Unit) {
        WriteCommandAction.runWriteCommandAction(handler.project) {
            command()
        }
        WriteCommandAction.runWriteCommandAction(handler.project) { // This WriteAction isn't really necessary?
            docManager.commitDocument(document) // There is difference with commitAllDocuments
        }
    }

    fun returnToOldState(state: CodeState) {
        runWriteCommandAndCommit {
            document.setText(state.code)
        } // Replace code with old one
        caret.moveToOffset(state.offset)
    }

    private fun getLinesAroundOffset(text: String, offset: Int) : CodePiece {
        val onLine = text.take(offset + 1).lines().size
        val lines = text.lines()
        val startLine = maxOf(onLine - GlobalStorage.linesAround, 0)
        val endLine = minOf(onLine + GlobalStorage.linesAround, lines.size)
        val code = lines.subList(startLine, endLine).joinToString(separator = "\n")
        var startOffset = 0
        for (i in 0 until startLine) startOffset += lines[i].length + 1
        var endOffset = startOffset
        for (i in startLine until endLine) {
            endOffset += lines[i].length + 1
        }
        return CodePiece(-1, code, minOf(onLine + 1, GlobalStorage.linesAround), startOffset, endOffset - 1, offset, "", text)
//        return Pair(minOf(onLine + 1, GlobalStorage.linesAround), code)
    }

    fun start(depth: Int = 0, max_depth: Int = 5){
        if (depth > max_depth) return
        val actions = handler.getIntentionsList(true)

        val oldState = CodeState(document.text, caret.offset)

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
                start(depth + 1, max_depth)
            }
            returnToOldState(oldState)
        }
//        if (depth == 0) {
//            EditorFactory.getInstance().releaseEditor(handler.editor)
//        }

    }

    fun dumpHashMap(parentFilename: String, path: String, filename: String) { // Write our map to file
        val file = File("$out_path/maps/$parentFilename/$filename.json")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs()
        if (!file.exists())
            file.createNewFile()
        val codePieces = mutableListOf<CodePiece>()
        hashes.forEach {
            it.value.hash = it.key
            it.value.path = path
            codePieces.add(it.value)
        }

        file.writeText(Json{prettyPrint = true}.encodeToString(codePieces))
    }

    fun getCodePieces(): MutableCollection<CodePiece> {
        val codePieces = mutableListOf<CodePiece>()
        hashes.forEach {
            it.value.hash = it.key
            it.value.path = "*Skipped value*"
            codePieces.add(it.value)
        }
        return codePieces
    }


}