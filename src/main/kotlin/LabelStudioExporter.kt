import graph.Graph
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
class LabelStudioData(val first: String, val second: String, val ref_id: Int, val pathId : Int)

@Serializable
class LabelStudioInputElement(val data: LabelStudioData) // For some reason library doesn't allow creation of data inside of constructor

object LabelStudioExporter {
    private fun sanitizePath(path: String) : String {
        return path.replace("\t", " ").replace("\n", " ")
    }
    fun export() {
        val pathIndexToCode = mutableMapOf<Int, MutableList<LabelStudioInputElement>>()
        val usedPieces = mutableMapOf<Pair<Int, Int>, Int>() // Two hashes and their ref_id to have every pair only once
        val linkageFile = File("${GlobalStorage.out_path}/labelStudioFiles/linkage.tsv")
        linkageFile.delete()
        linkageFile.appendText("id\tfile\tfirst_hash\tsecond_hash\tref_id\tpath_id\n")
        var id = 0
        val elements = mutableListOf<LabelStudioInputElement>()
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach {
            val graph = Graph()
            // Get folder name and change extension
            var fileName = it.absolutePath.split("/").takeLast(2).joinToString("/").split(".").dropLast(1).joinToString(".") + ".dot"

            graph.build(File("${GlobalStorage.out_path}/dots/$fileName"))
            graph.bfs()
            val codePieces: List<CodePiece> = Json.decodeFromString(it.readText())
            val originalVariant = codePieces.first() // Here we rely on default implementations of Map and List which preserves insertion order
            for (codePiece in codePieces.drop(1)) {
                val pair = Pair(originalVariant.code.hashCode(), codePiece.code.hashCode())
                val pathId = graph.nodes[codePiece.hash]!!.pathIndex
                if (pair in usedPieces.keys) {
                    linkageFile.appendText("${id}\t${sanitizePath(it.absolutePath)}\t${originalVariant.hash}\t${codePiece.hash}\t${usedPieces[pair]}\t$pathId\n")
                    id++
                    continue
                } else {
                    val el = LabelStudioInputElement(
                        LabelStudioData(
                            addPrimers(
                                originalVariant.code,
                                originalVariant.intentionLine
                            ), addPrimers(codePiece.code, codePiece.intentionLine), id, pathId
                        )
                    )
                    // Tab replacement just in case of their presence in file name
                    linkageFile.appendText(
                        "${id}\t${sanitizePath(it.absolutePath)}\t${originalVariant.hash}\t${codePiece.hash}\t$id\t$pathId\n"
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
        for ((key, value) in pathIndexToCode) {
            println("$key ${value.size} ${Graph.Mappings.indexToPathMapping[key]}")
            elements.addAll(value.shuffled().take(GlobalStorage.samplesForEachPath))
        }
        val nGroups = 1
        if (nGroups == 1) {
            File("${GlobalStorage.out_path}/labelStudioFiles/sample.json").writeText(Json {
                prettyPrint = true
            }.encodeToString(elements))
            print(elements.size)
        } else {
            divideToNGroups(pathIndexToCode, nGroups)
        }
        File("${GlobalStorage.out_path}/labelStudioFiles/pathIndexToPath.json").writeText(Json {
            prettyPrint = true
        }.encodeToString(Graph.Mappings.indexToPathMapping))
    }

    private fun divideToNGroups(elements: MutableMap<Int, MutableList<LabelStudioInputElement>>, nGroups: Int) {
        val sorted = elements.toList().sortedBy { (_, value) -> value.size }
        val newList = mutableListOf<Pair<Int, List<LabelStudioInputElement>>>()
        var sum = 0
        for (i in sorted.indices) {
            val subset = sorted[i].second.shuffled().take(GlobalStorage.samplesForEachPath)
            sum+=subset.size
            newList.add(Pair(sorted[i].first, subset))
        }
        val inEachGroup = sum / nGroups
        newList.reverse()
        for (i in 1..nGroups) {
            var sum = 0
            val sample = mutableListOf<Pair<Int, List<LabelStudioInputElement>>>()
            bigLoop@for (i in newList.indices) {
                if (sum + newList[i].second.size <= inEachGroup) { // Greedy add biggest elements
                    sum += newList[i].second.size
                    sample.add(newList[i])
                } else {
                    for (j in i until newList.size) { // Find best small element
                        if (sum + newList[j].second.size <= inEachGroup) {
                            sum += newList[j].second.size
                            sample.add(newList[j])
                            break@bigLoop
                        }
                    }
                }
            }
            val result = mutableListOf<LabelStudioInputElement>()
            println(i)
            for (pair in sample) {
                newList.remove(pair)
                result.addAll(pair.second)
                println("${pair.first} ${pair.second.size} ${Graph.Mappings.indexToPathMapping[pair.first]}")
            }
            if (i == nGroups) {
                for (el in newList) {
                    result.addAll(el.second)
                }
            }
            File("${GlobalStorage.out_path}/labelStudioFiles/samples/sample_$i.json").writeText(Json {
                prettyPrint = true
            }.encodeToString(result))
        }
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