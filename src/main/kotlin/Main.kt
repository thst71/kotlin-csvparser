import de.beanfactory.csvparser.CsvParser
import java.nio.charset.StandardCharsets
import kotlin.io.path.readBytes

fun main(args: Array<String>) {
    println("Loading file ${args[0]}")

    val csvFile =
        String(kotlin.io.path.Path(args[0]).readBytes(), StandardCharsets.UTF_8)

    val csvRows = CsvParser().parse(csvFile)
}