package metrics

import CodePiece
import com.intellij.psi.PsiFile

interface Metric {

    val name: String

    fun calculate(psiFile: PsiFile, codePiece: CodePiece) : Float?
}