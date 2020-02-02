package firelib.core.report

import java.nio.file.Paths
import java.time.Instant


data class TTT(val name : String, val intVal : Int, val longVal : Long, val inst : Instant)

fun main() {
    val writer = GeGeWriter("ttt3", Paths.get("/home/ivan/tmp/test"), TTT::class, listOf("name"))

    writer.write(listOf(TTT("B", 0, 2L, Instant.now())))

    println(writer.read())

}