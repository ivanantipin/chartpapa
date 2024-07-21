package com.firelib.techbot.divreader

import com.firelib.techbot.command.CacheRecord
import com.firelib.techbot.command.CacheService
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.time.Duration
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class Div(val ticker: String, val lastDayWithDivs: LocalDate, val div: Double, val status : String)

object DivReader {


    fun fetchDivs(ticker : String) : List<Div>{

        val url = "https://www.dohod.ru/ik/analytics/dividend/${ticker.lowercase()}"

        val key = "${ticker}_divs"
        val record = CacheService.getRecord(key)

        var updateCache = false;
        val doc = if(record != null ){
            val docStr = String(record.data)
            if(docStr == "FAILED"){
                println("doc marked as failed for ticker ${ticker}, skipping")
                return emptyList()
            }
            Jsoup.parse(String(record.data))
        }else{
            try {
                updateCache = true
                Jsoup.connect(url).get()
            }catch (e : Exception){
                val time = System.currentTimeMillis()
                CacheService.updateRecord(CacheRecord(key, time + Duration.ofDays(2).toMillis(), time, "FAILED".toByteArray()))
                null
            }
        }
        if(doc == null) return emptyList()

        if(updateCache){
            val time = System.currentTimeMillis()
            CacheService.updateRecord(CacheRecord(key, time + Duration.ofDays(2).toMillis(), time, doc.toString().toByteArray()))
        }

        val tables = doc.select("table.content-table")

        val find: List<List<String>> = tables.map({
            parseTable(it)
        }).find { it.first().size == 4 }!!

        return find.drop(1).map {
            try {
                val date = LocalDate.parse(it[1], DateTimeFormatter.ofPattern("dd.MM.yyyy"))
                Div(ticker, date, it[3].toDouble(), "OK")
            }catch (e : Exception){
                null
            }
        }.filterNotNull()
    }

    private fun parseTable(table: Element): List<List<String>> {
        val rows = table.select("tr")

        return rows.map { row ->
            row.select("td, th").map { cell ->
                cell.text().trim()
            }
        }

    }

}

fun main() {

    val allDivs = DivReader.fetchDivs("MTSS")

    allDivs.forEach {
        println(it)
    }


//    val writer = GeGeWriter<Div>(GlobalConstants.metaDb, Div::class, listOf("ticker", "LastDayWithDivs"), "open_divs")
//
//    writer.write(allDivs)
}