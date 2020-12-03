object GlobalStorage {
    val linesAround = 5
    val samplesForEachPath = 30
    var out_path: String? = null //TODO remove all paths later since it is only for development
    val usedIntentions = mutableSetOf<String>()
}