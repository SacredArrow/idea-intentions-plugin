import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import java.io.File

// Checks, how many intentions there are in every file
class StatisticsGatherer {
    private var outputFile: File? = null
    fun gather(path: String) { // This path is the path files were created for. We only need it to extract filename.
        val filename = path.split("/").last().split(".")[0] + ".stat" // Get only filename and change extension
        outputFile = File("${GlobalStorage.out_path}/statistics/$filename")
        outputFile!!.delete()
        val directory = File("${GlobalStorage.out_path}/maps")
        directory.walk().filter { it.isFile }.forEach { getStatisticsFromFile(it) }
    }

    private fun getStatisticsFromFile(file: File) {
        val codePieces: List<CodePiece> = Json.decodeFromString(file.readText())
        outputFile!!.appendText("${file.absolutePath}, ${codePieces.size}\n")

    }
}