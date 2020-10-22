import IntentionHandler.Companion.editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class FileApplier {
    private val usedPositions = HashSet<Int>()
    fun start() {
        val file  = IntentionHandler.file!!.copy() // File in editor should be consistent with this one
        val dotPrinter = IntentionListToDot()
        println(file)
        val currentFilename = IntentionHandler.file!!.virtualFile.name
        println(currentFilename)
        file.accept(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                println(element)
                val offset = element.textOffset
                if (offset != 0 && offset !in usedPositions) {
                    usedPositions.add(offset) // Consider each position only once
                    println(offset)
                    editor!!.caretModel.moveToOffset(offset)
                    val applier = SequentialApplier()
                    applier.start()
                    val fileName = "$offset - $element"

                    if (dotPrinter.process(applier.events, currentFilename, fileName)) {
                        applier.dumpHashMap(currentFilename, fileName)
                    }
                }
                super.visitElement(element)
            }
        })
    }
}