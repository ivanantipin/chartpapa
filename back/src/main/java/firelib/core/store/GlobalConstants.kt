package firelib.core.store

import firelib.core.report.SqlQueries
import org.apache.commons.io.FileUtils
import java.nio.file.Paths
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

object GlobalConstants {

    val env = System.getProperty("env") ?: System.getenv("env") ?: "test"

    val rootFolder = Paths.get("/ddisk/globaldatabase")

    val mdFolder = rootFolder.resolve("md")

    val imgFolder = mdFolder.resolve("img")

    val metaDb = mdFolder.resolve("${env}.db")

    val props = SqlQueries.readProps(metaDb, "envs", env)

    fun getProp(name: String): String {
        if(System.getProperty(name) != null){
            return System.getProperty(name)
        }
        require(props.containsKey(name), { "prop ${name} is absent" })
        return props[name]!! as String
    }

    val locks = ConcurrentHashMap<String,Lock>()
    fun lock(id : String) : Lock{
        return locks.computeIfAbsent(id, {
            ReentrantLock()
        })
    }


    val rootReportPath = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out")

    fun ensureDirsExist() {
        FileUtils.forceMkdir(mdFolder.toFile())
        FileUtils.forceMkdir(imgFolder.toFile())
        FileUtils.forceMkdir(rootReportPath.toFile())
    }

}