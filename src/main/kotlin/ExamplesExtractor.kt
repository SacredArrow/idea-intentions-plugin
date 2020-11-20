import com.intellij.codeInsight.intention.impl.config.IntentionManagerSettings
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class ExamplesExtractor {
    private val map = mutableMapOf<String, Int>()
    fun start() {
        var nJavaIntentions = 0
        var nExamplesWithOneExample = 0
        var nExamplesWithSpot = 0
        if (!File("${GlobalStorage.out_path}/referenceExamples").exists()) {
            File("${GlobalStorage.out_path}/referenceExamples").mkdir()
        }
        for (data in IntentionManagerSettings.getInstance().metaData) {
            val name = data.family.replace("/", "*slash*").replace("\\", "*backslash*") // Equals .action.familyName
            println(name)
            println(data.myCategory[0])
            if (data.myCategory[0] == "Java") {
                nJavaIntentions++
            } else continue
            try {
                if (data.exampleUsagesBefore.size == 1) nExamplesWithOneExample++
                val example= data.exampleUsagesBefore[0] // For simplicity and because 143/144 Java intentions contain only one example
                val ix = example.text.indexOf("<spot>")
                if (ix != -1)  {
                    nExamplesWithSpot++
                } else {
                    continue
                }
                val str = example.text.replace("<spot>", "").replace("</spot>", "")
                map[name] = ix
                File("${GlobalStorage.out_path}/referenceExamples/${name}.java").writeText(str)

            } catch (e: RuntimeException) {
                println("Some runtime exception occurred!")
            }
        }
        println("Number of intentions: ${IntentionManagerSettings.getInstance().metaData.size}")
        println("Number of Java intentions: $nJavaIntentions")
        println("Number of intentions with only one example: $nExamplesWithOneExample")
        println("Number of intentions with spot in it: $nExamplesWithSpot")
        File("${GlobalStorage.out_path}/referenceExamples/spots.json").writeText(Json{prettyPrint = true}.encodeToString(map))
    }
}