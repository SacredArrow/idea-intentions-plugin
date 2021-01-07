package metrics

import CodePiece
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile

class NumberOfParametersMetric : Metric {
    override val name: String = "Number of parameters"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        var result : Float = (-1).toFloat();
        psiFile.accept(object : JavaRecursiveElementVisitor() {
            override fun visitCallExpression(callExpression: PsiCallExpression) {
                // TODO walk only on part of file (get eldest element by offset)
                if (codePiece.offset in callExpression.textRange.startOffset..callExpression.textRange.endOffset) { // Careful with nested calls
                    println("Expression in range at offset " + callExpression.textRange.startOffset)
                    if (callExpression.argumentList != null) {
                        result = callExpression.argumentList!!.expressionCount.toFloat()
                        return
                    }
                }
            }
        })
        return if (result < 0) null else result
    }
}