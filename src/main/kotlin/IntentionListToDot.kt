import java.io.File

object IntentionListToDot {
    fun process(events: List<IntentionEvent>, filename: String) {
        val builder = StringBuilder()
        builder.append("digraph G {\n")
        for (event in events) {
            builder.append("${event.hash_start} -> ${event.hash_end} [label=\"${event.name}\"]\n")
        }
        builder.append("}")
        println(builder.toString())
        val file = File("${IntentionHandler.out_path}/dots/${filename}.dot")
        if (!file.parentFile.exists())
            file.parentFile.mkdirs();
        if (!file.exists())
            file.createNewFile();
        file.writeText(builder.toString())
    }
}