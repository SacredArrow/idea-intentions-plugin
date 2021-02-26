package metrics

import CodePiece
import com.intellij.psi.PsiFile

// This metric only has sense when being used as difference and should not be included in the features
class IndentationsNumber : Metric {
    override val name: String = "Number of indentations"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        var sum = 0
        codePiece.code.lines().forEach { sum += it.length - it.trim().length }
        return sum.toFloat()
    }
}