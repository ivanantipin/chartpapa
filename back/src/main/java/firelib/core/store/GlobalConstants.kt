package firelib.core.store

import org.apache.commons.io.FileUtils
import java.io.FileReader
import java.nio.file.Paths
import java.util.*

object GlobalConstants{

    val env = System.getProperty("env") ?:  System.getenv("env") ?: "test"


    val props = Properties().apply {
        println(" ENV set to  ${env}")
        load(FileReader("${System.getProperty("user.home")}/keys/${env}.properties"))
    }

    fun getProp(name : String ) : String{
        require(props.containsKey(name), {"prop ${name} is absent"})


        return props[name]!! as String
    }

    val mdFolder = Paths.get("/ddisk/globaldatabase/md")

    val metaDb = mdFolder.resolve("meta.db")

    val rootReportPath = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out")

    fun ensureDirsExist(){
        FileUtils.forceMkdir(mdFolder.toFile())
        FileUtils.forceMkdir(rootReportPath.toFile())
    }

}