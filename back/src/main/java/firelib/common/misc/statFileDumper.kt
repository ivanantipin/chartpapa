package firelib.common.misc

import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardOpenOption

fun appendRow(ff: String, row: String) {
    if (Files.exists(Paths.get(ff))) {
        Files.write(Paths.get(ff), listOf(row), StandardOpenOption.APPEND)
    } else {
        Files.write(Paths.get(ff), listOf(row), StandardOpenOption.CREATE)
    }
}

fun appendRows(ff: String, rows: List<String>) {
    Files.write(Paths.get(ff), rows, StandardOpenOption.APPEND)
}

fun writeRows(ff: String, rows: Iterable<String>) {
    Files.write(Paths.get(ff), rows, StandardOpenOption.CREATE)
}


