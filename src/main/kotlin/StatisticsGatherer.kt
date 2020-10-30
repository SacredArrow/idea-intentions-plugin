import java.io.File

class StatisticsGatherer(private val linesInNode: Int) {
    var outputFile: File? = null
    fun gather(path: String) { // This path is the path files were created for. We only need it to extract filename.
        val filename = path.split("/").last().split(".")[0] + ".stat" // Get only filename and change extension
        outputFile = File("${GlobalStorage.out_path}/statistics/$filename")
        val directory = File("${GlobalStorage.out_path}/maps")
        directory.walk().filter { it.isFile }.forEach { getStatisticsFromFile(it) }
    }

    private fun getStatisticsFromFile(file: File) {
        val size = file.readLines().size
        val nNodes = size / linesInNode
        outputFile!!.appendText("${file.absolutePath}, $nNodes\n")

    }
}