import com.intellij.openapi.project.DumbService
import com.intellij.openapi.project.ProjectManager
import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.psi.*
import com.intellij.psi.impl.PsiFileFactoryImpl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File


class MetricsCalculator {
    private fun calculateForFile(file: File) {
        val codePieces: List<CodePiece> = Json.decodeFromString(file.readText())
        val project = ProjectManagerImpl.getInstance().openProjects[0]
        for (codePiece in codePieces) {
            println("Next codepiece")
            val psiFile = PsiFileFactoryImpl(project).createFileFromText("dumb.java", codePiece.code )
            psiFile.accept(object : JavaRecursiveElementVisitor() {
                override fun visitMethod(method: PsiMethod) {
                    super.visitMethod(method)
                    println("Found a method at offset " + method.textRange.startOffset);
                }

                override fun visitLocalVariable(variable: PsiLocalVariable) {
                    super.visitLocalVariable(variable);
                    println("Found a variable at offset " + variable.textRange.startOffset);
                }
            })
            psiFile.accept(object: PsiRecursiveElementWalkingVisitor() {
                override fun visitElement(element: PsiElement) {
                    println(element.textRange)
                }}
            )

        }
    }
    fun calculate() {
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach { calculateForFile(it) }
    }
}