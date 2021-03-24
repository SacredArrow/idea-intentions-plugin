import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.DumbServiceImpl
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class FileApplier(private val handler: CurrentPositionHandler) {
    private val usedPositions = mutableSetOf<Int>()
    fun start() {
        val dotPrinter = IntentionListToDot()
        println(handler.file)
        val currentFilename = handler.file.virtualFile.name
        println(currentFilename)
        val offsetDict = mutableMapOf<Int, String>()
        handler.file.accept(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                println(element)
                var textedElement = ""
                var offset = 0
                ApplicationManager.getApplication().runReadAction {
                    offset = element.textOffset
                    textedElement = element.toString()
                }
                if (offset != 0 && offset !in offsetDict.keys) { // Consider each position only once
                    offsetDict[offset] = textedElement
                }
                ApplicationManager.getApplication().runReadAction {
                    super.visitElement(element)
                }
            }
        })
        for ((offset, name) in offsetDict) {
            println(offset)
            ApplicationManager.getApplication().invokeAndWait {
                handler.editor.caretModel.moveToOffset(offset)
            }
            val applier = SequentialApplier(handler) // Copy file to get rid of hash set with Actions
//                    if (applier.start()) { // Was it necessary for something?
            applier.start()
            var fileName = "$offset - $name"
            fileName = GlobalStorage.cleanFileName(fileName)
            if (dotPrinter.process(applier.events, currentFilename, fileName)) {
                applier.dumpHashMap(currentFilename, handler.file.virtualFile.path, fileName)
            }
        }
    }
}