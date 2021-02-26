package metrics

import CodePiece
import com.intellij.psi.PsiFile

// This metric only has sense when being used as difference and should not be included in the features
class NumberOfEmptyLinesMetric : Metric {
    override val name: String = "Number of empty lines"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        return codePiece.code.lines().filter { it.isEmpty() || it.isBlank() }.count().toFloat()
    }
}