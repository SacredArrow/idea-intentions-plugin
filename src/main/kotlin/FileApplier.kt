import IntentionHandler.Companion.editor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiRecursiveElementWalkingVisitor

class FileApplier {
    fun start() {
        val file  = IntentionHandler.file!!.copy() // File in editor should be consistent with this one
        println(file)
        file!!.accept(object: PsiRecursiveElementWalkingVisitor() {
            override fun visitElement(element: PsiElement) {
                println(element)
                val offset = element.textOffset
                if (offset != 0) {
                    println(offset)
                    editor!!.caretModel.moveToOffset(offset)
                    val applier = SequentialApplier()
                    applier.start()
                    val fileName = "$offset - $element"
                    applier.dumpHashMap(fileName)
                    IntentionListToDot.process(applier.events, fileName)
                }
                super.visitElement(element)
            }
        })
    }
}