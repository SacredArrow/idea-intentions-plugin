import java.io.File

class IntentionListToDot {
    private val hashes = HashSet<Int>()

    fun process(events: List<IntentionEvent>, parentFilename: String, filename: String): Boolean {
        if (events.isEmpty()) return false
        val builder = StringBuilder()
        builder.append("digraph G {\n")
        for (event in events) {
            builder.append("${event.hash_start} -> ${event.hash_end} [label=\"${event.name}\"]\n")
        }
        builder.append("}")
        val file = File("${IntentionHandler.out_path}/dots/$parentFilename/$filename.dot")
        val result = builder.toString()

        return if (result.hashCode() in hashes) {
            false
        } else {
            hashes.add(result.hashCode())
            if (!file.parentFile.exists())
                file.parentFile.mkdirs();
            if (!file.exists())
                file.createNewFile();
            file.writeText(result)
            true
        }

    }
}