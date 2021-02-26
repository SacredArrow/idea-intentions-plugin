package metrics

import CodePiece
import com.intellij.psi.PsiFile

// This metric is useful to detect splits by lines
class NumberOfLines : Metric {
    override val name: String = "Number of lines"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        return codePiece.fullCode.lines().size.toFloat()
    }
}