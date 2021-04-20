package metrics

import CodePiece
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil

class MaxLineLengthInsideExpression : Metric {
    override val name = "Max line length inside expression"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        var maxLength = 0
        ApplicationManager.getApplication().runReadAction {
            val element = psiFile.findElementAt(codePiece.offset)
            var containingCall: PsiCallExpression? = null

            containingCall = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)


            if (containingCall != null) {
                for (line in containingCall.text.lines()) {
                    if (line.length > maxLength) {
                        maxLength = line.length
                    }
                }
            }
        }
        return maxLength.toFloat()
    }
}