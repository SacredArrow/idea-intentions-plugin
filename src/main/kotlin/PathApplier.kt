import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiManager
import java.io.File
import kotlin.concurrent.thread

class PathApplier(private val handler: CurrentPositionHandler) {
    private var project = handler.project;
    fun start(path: String) {
        val file = File(path)
        var cnt = 0
        if (file.isFile) {
            if (file.extension == "sample") {
                File("${GlobalStorage.out_path}/dots").deleteRecursively()
                File("${GlobalStorage.out_path}/maps").deleteRecursively()
                val files = file.readLines()
//                project = ProjectManager.getInstance().loadAndOpenProject(files.first())!! // This line opens project, but it is bugged
//                DumbService.getInstance(project).smartInvokeLater {
                files.drop(1).forEach {
                    cnt += 1
                    val indicator = ProgressManager.getInstance().progressIndicator;
                    indicator.fraction = cnt.toDouble() / files.size
                    indicator.text = "File $cnt from ${files.size}"
                    startForFile(it)
                } // File with paths in it
//                }
            } else {
                startForFile(path)
            }
        } else {
            file.walk().filter { it.extension == "java" }.forEach { startForFile(it.absolutePath) }
        }
        for (el in GlobalStorage.usedIntentions) println(el)
        println("\nUnused:")
        for (el in handler.getIntentionsList(false)) {
            if (!GlobalStorage.usedIntentions.contains(el.familyName)) println(el.familyName)
        }
    }
    private fun startForFile(path: String) {
        val virtualFile = VirtualFileManager.getInstance().findFileByUrl("file://$path")!!
        lateinit var file : PsiFile
        ApplicationManager.getApplication().runReadAction {
            file = PsiManager.getInstance(project).findFile(virtualFile)!!
        }
        println(file)
        lateinit var document : Document
        ApplicationManager.getApplication().runReadAction {
            document = PsiDocumentManager.getInstance(project).getDocument(file)!!
        }
        lateinit var editor : Editor
        ApplicationManager.getApplication().invokeAndWait {
            editor = EditorFactory.getInstance().createEditor(document) // Must be invoked in EDT?
        }
        println(editor)
        println(document)
        val handler = CurrentPositionHandler(project, editor, file)
        val applier = FileApplier(handler)
        applier.start()
        ApplicationManager.getApplication().invokeAndWait {
            EditorFactory.getInstance().releaseEditor(editor)
        }

    }
}