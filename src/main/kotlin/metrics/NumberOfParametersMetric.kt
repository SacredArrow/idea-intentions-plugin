package metrics

import CodePiece
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class NumberOfParametersMetric : Metric {
    override val name: String = "Number of parameters"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        val element = psiFile.findElementAt(codePiece.offset)!!
        val containingCall : PsiCallExpression? = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)
        return containingCall?.argumentList?.expressionCount?.toFloat()
    }
}