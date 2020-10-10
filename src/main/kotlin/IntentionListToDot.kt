import java.io.File

class IntentionListToDot {
    fun process(events: List<IntentionEvent>) {
        val builder = StringBuilder()
        builder.append("digraph G {\n")
        for (event in events) {
            builder.append("${event.hash_start} -> ${event.hash_end} [label=\"${event.name}\"]\n")
        }
        builder.append("}")
        println(builder.toString())
        File("/home/custos/Projects/Diploma/out.dot").writeText(builder.toString())
    }
}