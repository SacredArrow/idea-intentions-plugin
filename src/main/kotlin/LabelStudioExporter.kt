import graph.Graph
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class LabelStudioData(val first: String, val second: String, val ref_id: Int)

@Serializable
class LabelStudioInputElement(val data: LabelStudioData) // For some reason library doesn't allow creation of data inside of constructor

object LabelStudioExporter {
    fun export() {
        val pathIndexToCode = mutableMapOf<Int, MutableList<LabelStudioInputElement>>()
        val usedPieces = mutableMapOf<Pair<Int, Int>, Int>() // Two hashes and their ref_id to have every pair only once
        val linkageFile = File("${GlobalStorage.out_path}/labelStudioFiles/linkage.tsv")
        linkageFile.delete()
        linkageFile.appendText("id\tfile\tfirst_hash\tsecond_hash\tref_id\n")
        var id = 0
        val elements = mutableListOf<LabelStudioInputElement>()
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach {
            val graph = Graph()
            val fileName = it.absolutePath.split("/").takeLast(2).joinToString("/").split(".").dropLast(1).joinToString(".") + ".dot"
            graph.build(File("${GlobalStorage.out_path}/dots/$fileName"))
            graph.bfs()
            val codePieces: List<CodePiece> = Json.decodeFromString(it.readText())
            val originalVariant = codePieces.first() // Here we rely on default implementations of Map and List which preserves insertion order
            for (codePiece in codePieces.drop(1)) {
                val pair = Pair(originalVariant.code.hashCode(), codePiece.code.hashCode())
                val pathId = graph.nodes[codePiece.hash]!!.pathIndex
                if (pair in usedPieces.keys) {
                    linkageFile.appendText("${id}\t${it.absolutePath.replace("\t", " ")}\t${originalVariant.hash}\t${codePiece.hash}\t${usedPieces[pair]}\n")
                    id++
                    continue
                } else {
                    val el = LabelStudioInputElement(
                        LabelStudioData(
                            addPrimers(
                                originalVariant.code,
                                originalVariant.intentionLine
                            ), addPrimers(codePiece.code, codePiece.intentionLine), id
                        )
                    )
                    // Tab replacement just in case of their presence in file name
                    linkageFile.appendText(
                        "${id}\t${
                            it.absolutePath.replace(
                                "\t",
                                " "
                            )
                        }\t${originalVariant.hash}\t${codePiece.hash}\t$id\n"
                    )
                    usedPieces[pair] = id
                    id++
                    if (pathId !in pathIndexToCode.keys) {
                        pathIndexToCode[pathId] = mutableListOf()
                    }
                    pathIndexToCode[pathId]!!.add(el)
                }
            }
        }
        for ((_, value) in pathIndexToCode) {
//            println("$key ${value.size}")
            elements.addAll(value.shuffled().take(GlobalStorage.samplesForEachPath))
        }
        File("${GlobalStorage.out_path}/labelStudioFiles/sample.json").writeText(Json{prettyPrint = true}.encodeToString(elements))
    }

    private fun addPrimers(code: String, intentionLine: Int): String { // This is needed to have a selection in Label Studio
        val codeLines = code.lines().toMutableList()
        codeLines[intentionLine - 1] = codeLines[intentionLine - 1] + "// End_of_unique_sequence_which_shouldnt_be_repeated"
        if (intentionLine == 1) {
            codeLines.add(0, "// Start_of_unique_sequence_which_shouldnt_be_repeated") // In case if it is the first line (Shouldn't normally be executed
        } else {
            codeLines[intentionLine - 2] = codeLines[intentionLine - 2] + "// Start_of_unique_sequence_which_shouldnt_be_repeated"
        }

        return codeLines.joinToString("\n")
    }

}