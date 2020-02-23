package firelib.core.store

import org.apache.commons.io.FileUtils
import java.io.FileReader
import java.nio.file.Paths
import java.util.*

object GlobalConstants{

    val env = System.getProperty("env") ?: "test"

    val props = Properties().apply {
        load(FileReader("${System.getProperty("user.home")}/keys/${env}.properties"))
    }

    fun getProp(name : String ) : String{
        require(props.containsKey(name), {"prop ${name} is absent"})
        return props[name]!! as String
    }

    val mdFolder = Paths.get("/ddisk/globaldatabase/md")

    val metaDb = mdFolder.resolve("meta.db")

    val rootReportPath = if(env == "test")
        Paths.get("/home/ivan/projects/chartpapa/market_research/report_out") else
        Paths.get(System.getProperty("user.dir"))

    fun ensureDirsExist(){
        FileUtils.forceMkdir(mdFolder.toFile())
        FileUtils.forceMkdir(rootReportPath.toFile())
    }

}