import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.extensions.ExtensionPointName
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.impl.ProjectManagerImpl
import com.intellij.psi.*
import com.intellij.psi.impl.PsiFileFactoryImpl
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import metrics.*
import java.io.File


class MetricsCalculator {

    private val project: Project = ProjectManagerImpl.getInstance().openProjects[0]
    private val epName : ExtensionPointName<Metric> = ExtensionPointName.create("my.plugin.metricsExtensionPoint");
    private val metricsFile = File("${GlobalStorage.out_path}/labelStudioFiles/metrics.tsv")

    private fun calculateForCodePiece(codePiece: CodePiece): MutableMap<String, Float?> {
        // We can't extract it from disk since it can be changed piece, so we create new file
        val psiFile = PsiFileFactoryImpl(project).createFileFromText("dumb.java", JavaFileType.INSTANCE, codePiece.fullCode)
        val metrics = mutableMapOf<String, Float?>()

        for (extension in epName.extensionList){
            metrics[extension.name] = extension.calculate(psiFile, codePiece)
        }
        return metrics
    }

    private fun calculateForFile(file: File) {
        println(file.name)
        val codePieces: List<CodePiece> = Json.decodeFromString(file.readText())
        for (codePiece in codePieces) {
            println("Next codepiece")
            println(codePiece.offset)
            check(codePiece.fullCode.subSequence(codePiece.codePieceStart, codePiece.codePieceEnd) == codePiece.code)
            val metrics = calculateForCodePiece(codePiece)
            metricsFile.appendText("${codePiece.path}\t${codePiece.hash}\t${codePiece.offset}")
            for ((key,value) in metrics) { // We rely on map and extensions order preservation
                println(key)
                metricsFile.appendText("\t$value")
            }
            metricsFile.appendText("\n")
        }
    }

    fun calculate() {
        metricsFile.delete()
        var metricNames = mutableSetOf<String>()
        for (extension in epName.extensionList){
            metricNames.add(extension.name)
        }
        metricsFile.appendText("path\thash\toffset\t")
        metricsFile.appendText(metricNames.joinToString(separator = "\t") + "\n")
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach { calculateForFile(it) }
    }
}