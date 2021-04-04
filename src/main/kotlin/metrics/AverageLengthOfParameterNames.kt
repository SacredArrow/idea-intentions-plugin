package metrics

import CodePiece
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class AverageLengthOfParameterNames : Metric {
    override val name: String = "Average length of parameter names"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        var result : Float = (-1).toFloat();
        var nParams = 0;
        ApplicationManager.getApplication().runReadAction {
            val element = psiFile.findElementAt(codePiece.offset)!!
            val containingCall: PsiCallExpression? = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)
            if (containingCall != null && containingCall.argumentList != null) {
                for (expression in containingCall.argumentList!!.expressions) {
                    result += expression.textLength
                    nParams++
                }
            }
        }
        return if (result < 0) null else result / nParams
    }
}