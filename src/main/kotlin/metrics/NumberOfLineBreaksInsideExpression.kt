package metrics

import CodePiece
import com.intellij.psi.PsiCallExpression
import com.intellij.psi.PsiFile

import com.intellij.psi.util.PsiTreeUtil




class NumberOfLineBreaksInsideExpression : Metric {
    override val name = "Number of line breaks inside expression"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float? {
        val element = psiFile.findElementAt(codePiece.offset)!!
        val containingCall: PsiCallExpression? = PsiTreeUtil.getParentOfType(element, PsiCallExpression::class.java)
        return containingCall?.text?.filter { it == '\n' }?.count()?.toFloat()
    }

}