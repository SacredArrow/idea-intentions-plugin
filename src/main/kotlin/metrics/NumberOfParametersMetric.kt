package metrics

import CodePiece
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class NumberOfParametersMetric : Metric {
    override val name: String = "Number of parameters"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        var result: Float? = 0.0f
        ApplicationManager.getApplication().runReadAction {
            val element = psiFile.findElementAt(codePiece.offset)
            val containingCall: PsiCallExpression? = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)
            result = containingCall?.argumentList?.expressionCount?.toFloat()
        }
        return result
    }
}