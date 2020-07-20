package firelib.core.store

import firelib.core.report.Sqls
import org.apache.commons.io.FileUtils
import java.nio.file.Paths


object GlobalConstants {

    val env = System.getProperty("env") ?: System.getenv("env") ?: "test"

    val rootFolder = Paths.get("/ddisk/globaldatabase")

    val mdFolder = rootFolder.resolve("md")

    val metaDb = mdFolder.resolve("${env}.db")

    val props = Sqls.readProps(metaDb, "envs", env)

    fun getProp(name: String): String {
        require(props.containsKey(name), { "prop ${name} is absent" })
        return props[name]!! as String
    }


    val rootReportPath = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out")

    fun ensureDirsExist() {
        FileUtils.forceMkdir(mdFolder.toFile())
        FileUtils.forceMkdir(rootReportPath.toFile())
    }

}