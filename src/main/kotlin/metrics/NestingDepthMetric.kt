package metrics

import CodePiece
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.PsiMethod




class NestingDepthMetric : Metric {
    override val name: String = "Nesting depth"

    override fun calculate(psiFile: PsiFile, codePiece: CodePiece): Float {
        val element = psiFile.findElementAt(codePiece.offset)!!
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java)
        return PsiTreeUtil.getDepth(element, containingMethod).toFloat()
    }
}