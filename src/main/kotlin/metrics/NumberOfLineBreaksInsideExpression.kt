package metrics

import CodePiece
import com.intellij.openapi.application.ApplicationManager
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile

import com.intellij.psi.util.PsiTreeUtil




class NumberOfLineBreaksInsideExpression : Metric {
    override val name = "Number of line breaks inside expression"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        var result : Float? = 0.0f
        ApplicationManager.getApplication().runReadAction {
            val element = psiFile.findElementAt(codePiece.offset)
            val containingCall: PsiCallExpression? = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)

            result = containingCall?.text?.filter { it == '\n' }?.count()?.toFloat()
        }
        return result
    }

}