object GlobalStorage {
    val linesAround = 5
    val samplesForEachPath = 150
    var out_path: String? = null //TODO remove all paths later since it is only for development
    val usedIntentions = mutableSetOf<String>()

    fun cleanFileName(fileName: String) : String {
        var result = fileName.replace("/", "*slash*")
        if (result.length > 100) { // Some filenames are too long
            result = result.substring(0,100)
        }
        return result


    }
}