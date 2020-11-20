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
        val linkageFile = File("${GlobalStorage.out_path}/labelStudioFiles/linkage.tsv")
        linkageFile.delete()
        linkageFile.appendText("id\tfile\tfirst_hash\tsecond_hash\n")
        var id = 0
        val elements = mutableListOf<LabelStudioInputElement>()
        File("${GlobalStorage.out_path}/maps").walk().filter { it.isFile }.forEach {
            val codePieces: List<CodePiece> = Json.decodeFromString(it.readText())
            val originalVariant = codePieces.first() // Here we rely on default implementations of Map and List which preserves insertion order
            for (codePiece in codePieces.drop(1)) {
                val el = LabelStudioInputElement(LabelStudioData(addTags(originalVariant.code), addTags(codePiece.code), id))
                // Tab replacement just in case of their presence in file name
                linkageFile.appendText("${id}\t${it.absolutePath.replace("\t", " ")}\t${originalVariant.hash}\t${codePiece.hash}\n")
                id++
                elements.add(el)
            }
        }
        File("${GlobalStorage.out_path}/labelStudioFiles/sample.json").writeText(Json{prettyPrint = true}.encodeToString(elements))
    }

    private fun addTags(s: String) : String {
        return "<code><pre>$s</pre></code>" // For code to be rendered correctly
    }
}