package firelib

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.core.report.dao.ColDef
import firelib.core.report.dao.ColDefDao
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import org.apache.commons.csv.CSVFormat
import org.apache.commons.csv.CSVRecord
import org.springframework.web.client.RestTemplate
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.URL
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

val outDb = GlobalConstants.rootFolder.resolve("edgar.db")

fun updateTickerCik(): JsonNode {
    val template = RestTemplate()
    val str = template.getForObject("https://www.sec.gov/files/company_tickers.json", String::class.java)
    var mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KotlinModule())
    return mapper.readTree(str)
}


val map = mutableMapOf<String, ColDefDao<Map<String, String>>>()

fun importFile(linesIn: List<CSVRecord>, tableIn: String) {

    val table = tableIn.replace('.', '_')

    val maps = linesIn.map { it.toMap() }

    val dao = map.computeIfAbsent(tableIn, {
        val colDefs = maps.first().keys.map { hh ->
            ColDef<Map<String, String>, Any>(hh, { row -> row[hh]!! }, getType(hh))
        }.toTypedArray() as Array<ColDef<Map<String, String>, out Any>>
        ColDefDao(outDb, colDefs, table, getPk(table))
    })
//https://data.sec.gov/api/xbrl/companyfacts/CIK0001293135.json
//https://data.sec.gov/api/xbrl/frames/us-gaap/AccountsPayableCurrent/USD/CY2021Q2I.json"
    dao.upsert(maps)
}

fun unzip(url: String) {
    URL(url).openConnection().getInputStream().use { istream ->
        ZipInputStream(istream).use { zstream ->
            var localFileHeader: ZipEntry? = zstream.getNextEntry()

            while (localFileHeader != null) {
                if (localFileHeader.name.endsWith("txt")) {

                    val reader = BufferedReader(InputStreamReader(zstream))

                    val records: Iterable<CSVRecord> = CSVFormat.DEFAULT
                        .withFirstRecordAsHeader()
                        .withDelimiter('\t')
                        .withQuote(null)
                        .parse(reader)

                    records.chunked(10000).forEach { recs ->
                        importFile(recs, localFileHeader!!.name)
                        println("lines imported ${recs.size} from ${localFileHeader!!.name}")
                    }


                }
                localFileHeader = zstream.getNextEntry()
            }
        }
    }
}


data class EdgarCik(val cik: String, val ticker: String, val name: String)

fun main() {

    //

    val writer = GeGeWriter(outDb, EdgarCik::class, listOf("cik"), "cik_ticker")


//    val ciks = updateTickerCik()
//
//    writer.write(ciks.elements().asSequence().map {
//        println(it)
//        println(it["cik_str"])
//        EdgarCik(it["cik_str"].asText()!!, it["ticker"].textValue(), it["title"].textValue())
//    }.toList())


//    for(i in 0 until 10_000_000){
//        if(ciks["${i}"] != null){
//            writer.write(listOf(EdgarCik(cik = it["cik_str"])))
//            println(ciks["${i}"])
//        }else{
//            break
//        }
//    }


    for (year in 2020 until 2021) {
        for (q in 1..4) {
            unzip("https://www.sec.gov/files/dera/data/financial-statement-data-sets/${year}q${q}.zip")
        }
    }


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
