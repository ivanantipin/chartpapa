package firelib.core.store

import java.nio.file.Paths

object GlobalConstants{
    val mdFolder = Paths.get("/ddisk/globaldatabase/md")

    val metaDb = mdFolder.resolve("meta.db")

    val rootReportPath =
        Paths.get("/home/ivan/projects/chartpapa/market_research/report_out")
}