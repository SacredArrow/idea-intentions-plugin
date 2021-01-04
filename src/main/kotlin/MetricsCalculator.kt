import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.psi.*
import com.intellij.psi.impl.PsiFileFactoryImpl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


class MetricsCalculator {
    private fun calculateForFile(file: File) {
        println(file.name)
        val codePieces: List<CodePiece> = Json.decodeFromString(file.readText())
        val project = ProjectManagerImpl.getInstance().openProjects[0]
        for (codePiece in codePieces) {
            println("Next codepiece")
            val psiFile = PsiFileFactoryImpl(project).createFileFromText("dumb.java", JavaFileType.INSTANCE, "class Dumb {\npublic static void dumb() {" + codePiece.code + "}}")
            println(psiFile.text)
            println(codePiece.codeShift)
            println(codePiece.offset)
            psiFile.accept(object : JavaRecursiveElementVisitor() {
                override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                    println("Found a call at offset " + expression.textRange.startOffset);
                    println(expression.argumentList.expressionCount)
                    super.visitMethodCallExpression(expression)
                }
                override fun visitMethod(method: PsiMethod) {
                    println("Found a method at offset " + method.textRange.startOffset);
                    super.visitMethod(method);
                }

                override fun visitLocalVariable(variable: PsiLocalVariable) {
                    println("Found a variable at offset " + variable.textRange.startOffset);
                    super.visitLocalVariable(variable);
                }
            })
            psiFile.accept(object: PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
//                    println(element.textRange)
                    super.visitElement(element)
                }}
            )

        }
    }
    fun calculate() {
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach { calculateForFile(it) }
    }
}