package firelib

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.core.report.dao.ColDef
import firelib.core.report.dao.ColDefDao
import firelib.core.report.dao.GeGeWriter
import org.apache.commons.io.IOUtils
import org.springframework.web.client.RestTemplate
import java.net.URL
import java.nio.file.Paths
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.reflect.KType
import kotlin.reflect.full.createType


object EdgarImporter {
}


fun getPk(table: String): List<String> {
    return when {
        table.startsWith("sub") -> listOf("adsh")
        table.startsWith("num") -> listOf("adsh", "tag", "version", "ddate", "qtrs")
        else -> emptyList()
    }
}

fun getType(colName: String): KType {
    if (colName == "value") {
        return Double::class.createType()
    }
    return String::class.createType()
}

val outDb = Paths.get("/home/ivan/projects/chartpapa/market_research/edgar/data/edgar.db")

fun updateTickerCik(): JsonNode {
    val template = RestTemplate()
    val str = template.getForObject("https://www.sec.gov/files/company_tickers.json", String::class.java)
    var mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KotlinModule())
    return mapper.readTree(str)
}

fun importFile(linesIn: List<String>, tableIn: String) {

    val table = tableIn.replace('.','_')

    val lines = linesIn.map { it.split('\t') }

    val header = lines.first()
    val linesWithoutHeader = lines.subList(1, lines.size)



    val maps = linesWithoutHeader.map { lst ->
        header.mapIndexed { idx, it ->
            it to lst[idx]
        }.toMap()
    }

    val colDefs = header.map { hh ->
        ColDef<Map<String, String>, Any>(hh, { row -> row[hh]!! }, getType(hh))
    }.toTypedArray() as Array<ColDef<Map<String, String>, out Any>>

    val dao = ColDefDao(outDb, colDefs, table, getPk(table))

    dao.upsert(maps)
}

fun unzip(url : String) {
    URL(url).openConnection().getInputStream().use { istream ->
        ZipInputStream(istream).use { zstream ->
            var localFileHeader: ZipEntry? = zstream.getNextEntry()
            while (localFileHeader != null) {
                val lines = IOUtils.readLines(zstream)
                if(localFileHeader.name.endsWith("txt") && localFileHeader.name.startsWith("sub")){
                    importFile(lines as List<String>, localFileHeader.name)
                    println("lines imported ${lines.size} from ${localFileHeader.name}")
                }
                localFileHeader = zstream.getNextEntry()
            }
        }
    }
}

data class EdgarCik(val cik : String, val ticker : String, val name : String)

fun main() {

    //

    val writer = GeGeWriter(outDb, EdgarCik::class, listOf("cik"), "cik_ticker")


    val ciks = updateTickerCik()

    writer.write(ciks.elements().asSequence().map {
        println(it)
        println(it["cik_str"])
        EdgarCik(it["cik_str"].asText()!!, it["ticker"].textValue(), it["title"].textValue())
    }.toList())


//    for(i in 0 until 10_000_000){
//        if(ciks["${i}"] != null){
//            writer.write(listOf(EdgarCik(cik = it["cik_str"])))
//            println(ciks["${i}"])
//        }else{
//            break
//        }
//    }



//    for(year in 2015 until 2021){
//        for(q in 1 .. 4){
//            unzip("https://www.sec.gov/files/dera/data/financial-statement-data-sets/${year}q${q}.zip")
//        }
//    }



//        val dsForFile = SqlUtils.getDsForFile("/home/ivan/projects/chartpapa/market_research/edgar/data/edgar.db")
//
//        val allNames = JdbcTemplate(dsForFile).queryForList("select name from sub_txt", String::class.java).toSet()
//
//        //select * from sub_txt where name like '%apple%';
//
//        println(IqFeedHistoricalSource().symbols().filter {
//            allNames.contains(it.name.toUpperCase())
//        }.size)

//    importFile("/home/ivan/projects/chartpapa/market_research/edgar/data/pre.txt")
//    importFile("/home/ivan/projects/chartpapa/market_research/edgar/data/num.txt")
//    importFile("/home/ivan/projects/chartpapa/market_research/edgar/data/sub.txt")
//    importFile("/home/ivan/projects/chartpapa/market_research/edgar/data/tag.txt")
}
