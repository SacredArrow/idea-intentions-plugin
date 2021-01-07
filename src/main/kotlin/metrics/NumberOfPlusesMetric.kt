package metrics

import CodePiece
import com.intellij.psi.PsiFile

class NumberOfPlusesMetric : Metric {
    override val name: String = "Number of pluses"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        return codePiece.code.filter { it == '+' }.count().toFloat()
    }
}