import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class ProjectInfo(val name: String, val nUnique: Int, val intentions: Set<String>)

// Checks, how many intentions there are in every file
class StatisticsGatherer {
    private var outputFile: File? = null
    private var outputFile2: File? = null
    private var name: String = ""
    private val usedIntentions = mutableSetOf<String>()
    fun gather(path: String) { // This path is the path files were created for. We only need it to extract filename.
        name = path.split("/").last().split(".")[0] // Only filename

        outputFile = File("${GlobalStorage.out_path}/statistics/$name.stat")
        outputFile!!.delete()
        outputFile2 = File("${GlobalStorage.out_path}/statistics/$name.stat2")
        outputFile2!!.delete()

        val directory = File("${GlobalStorage.out_path}/maps")
        directory.walk().filter { it.isFile }.forEach { getStatisticsFromMapFile(it) }
        val directory2 = File("${GlobalStorage.out_path}/dots")
        directory2.walk().filter { it.isFile }.forEach { getStatisticsFromDotFile(it) }
        val info = ProjectInfo(name, usedIntentions.size, usedIntentions)
        outputFile2!!.writeText(Json{prettyPrint = true}.encodeToString(info))
    }

    private fun getStatisticsFromMapFile(file: File) {
        val codePieces: List<CodePiece> = Json.decodeFromString(file.readText()) // Read maps and process
        outputFile!!.appendText("${file.absolutePath}, ${codePieces.size}\n")

    }

    // This function uses the fact that every connection in dot file is on separate line
    private fun getStatisticsFromDotFile(file: File) {
        file.forEachLine {
            val split = it.split("[label=\"") // TODO Change to regexp
            if (split.size == 2) {
                usedIntentions.add(split[1].dropLast(2))
            }
        }

    }
}