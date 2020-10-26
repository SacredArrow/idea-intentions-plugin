import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiManager
import java.io.File

class PathApplier(handler: CurrentPositionHandler) {
    private val project = handler.project;
    fun start(path: String) {
        val file = File(path)
        if (file.isFile) {
            startForFile(path)
        } else {
            file.walk().filter { it.extension == "java" }.forEach { startForFile(it.absolutePath) }
        }
    }
    private fun startForFile(path: String) {
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path")!!
        val file = PsiManager.getInstance(project).findFile(virtualFile)
        println(file)
        val document = PsiDocumentManager.getInstance(project).getDocument(file!!)
        var editor: Editor? = null
        WriteCommandAction.runWriteCommandAction(project) {
            editor = EditorFactory.getInstance().createEditor(document!!) // Must be invoked in EDT?
        }
        println(editor)
        println(document)
        val handler = CurrentPositionHandler(project, editor!!, file)
        val applier = FileApplier(handler)
        applier.start()

    }
}