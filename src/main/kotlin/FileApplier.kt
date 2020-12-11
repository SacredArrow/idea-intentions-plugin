import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class FileApplier(private val handler: CurrentPositionHandler) {
    private val usedPositions = mutableSetOf<Int>()
    fun start() {
        val file  = handler.file.copy() // File in editor should be consistent with this one
        val dotPrinter = IntentionListToDot()
        println(file)
        val currentFilename = handler.file.virtualFile.name
        println(currentFilename)
        file.accept(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                println(element)
                val offset = element.textOffset
                if (offset != 0 && offset !in usedPositions) {
                    usedPositions.add(offset) // Consider each position only once
                    println(offset)
                    handler.editor.caretModel.moveToOffset(offset)
                    val applier = SequentialApplier(handler) // Copy file to get rid of hash set with Actions
                    if (applier.start()) {
                        var fileName = "$offset - $element"
                        fileName = GlobalStorage.cleanFileName(fileName)
                        if (dotPrinter.process(applier.events, currentFilename, fileName)) {
                            applier.dumpHashMap(currentFilename, fileName)
                        }
                    }
                }
                super.visitElement(element)
            }
        })
    }
}