package firelib.common.misc

import java.nio.file.{Files, Paths, StandardOpenOption}

import scala.collection.JavaConversions._


object statFileDumper {
    def appendRow(ff: String, row: String) {
        if (Files.exists(Paths.get(ff))) {
            Files.write(Paths.get(ff), List(row).toStream, StandardOpenOption.APPEND)
        } else {
            Files.write(Paths.get(ff), List(row).toStream, StandardOpenOption.CREATE)
        }
    }

    def appendRows(ff: String, rows: Seq[String]) {
        Files.write(Paths.get(ff), rows.toStream, StandardOpenOption.APPEND)
    }

    def writeRows(ff: String, rows: Iterable[String]) {
        Files.write(Paths.get(ff), rows.toStream, StandardOpenOption.CREATE)
    }

}
