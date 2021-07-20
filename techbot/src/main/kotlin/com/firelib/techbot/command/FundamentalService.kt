package com.firelib.techbot.command

import com.fasterxml.jackson.databind.JsonNode
import com.firelib.techbot.chart.Series
import com.firelib.techbot.initDatabase
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.readJson
import org.jetbrains.exposed.sql.transactions.transaction
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.util.*


object FundamentalService {

    fun fetch(ticker: String): String {
        return String(FundamentalService::class.java.getResourceAsStream("/cvx.json").readAllBytes())
    }

    fun fetchRest(ticker: String): String {
        val cached = CacheService.getCached("/fundamentals/${ticker}", {
            val template = RestTemplate()
            val url = "https://eodhistoricaldata.com/api/fundamentals/${ticker}.US?api_token=5e81907493e611.25433232"
            template.getForEntity(url, String::class.java).body.toByteArray()
        }, Interval.Week.durationMs)
        return String(cached)
    }

    fun getByInstrId(instrId: InstrId): List<Series<String>> {
        val json = fetchRest(instrId.code).readJson()
        return mergeSort(
            listOf(
                extractFromIncome(json, "operatingIncome"),
                extractFromIncome(json, "costOfRevenue"),
                extractFromIncome(json, "totalOperatingExpenses")
            )
        ).map { it.mapToStr() }
    }

    fun getFcfToDebt(instrId: InstrId): List<Series<String>> {
        val json = fetchRest(instrId.code).readJson()
        val cashFlow = extractFromCashFlow(json, "freeCashFlow")
        val debt = extractFromBs(json, "netDebt")
        val merged: List<Series<LocalDate>> = mergeSort(listOf(cashFlow, debt))
        val ndata = merged[0].data.mapValues { e ->
            merged[0].data[e.key]!! / merged[1].data[e.key]!!
        }
        return listOf(Series("fcfToDebt", ndata).mapToStr(), merged[1].mapToStr())
    }

    fun extractFromBs(json: JsonNode, name: String): Series<LocalDate> {
        val data = json["Financials"]["Balance_Sheet"]["quarterly"]
        return extractSeries(data, name)
    }

    fun extractNetIncome(json: JsonNode): SortedMap<LocalDate, Double> {
        return json["Income_Statement"]["quarterly"].map {
            LocalDate.parse(it["date"].textValue()) to it["netIncome"].textValue().toDouble()
        }.sortedBy { it.first }.takeLast(16).associateBy({ it.first }, { it.second }).toSortedMap()
    }

    fun <T : Comparable<T>> mergeSort(list: List<Series<T>>, lastN: Int = 16): List<Series<T>> {
        val set = list[0].data.keys.toMutableSet()

        list.forEach {
            set.retainAll(it.data.keys)
        }
        val last = set.sortedBy { it }.takeLast(lastN)
        return list.map {
            Series<T>(it.name, it.data.filterKeys { k -> last.contains(k) }.toSortedMap())
        }
    }


    fun LocalDate.toQuarter(): Long {
        return Math.round((this.dayOfYear / 365.0) * 4.0)
    }

    fun extractFromCashFlow(json: JsonNode, name: String): Series<LocalDate> {
        val data = json["Financials"]["Cash_Flow"]["quarterly"]
        return extractSeries(data, name)
    }

    private fun extractSeries(
        data: JsonNode,
        name: String
    ): Series<LocalDate> {
        val ret = data.filter { it["date"] != null && it[name].textValue() != null }.associateBy(
            {
                LocalDate.parse(it["date"].textValue())
            },
            {
                it[name].textValue().toDouble()
            }
        )
        return Series(name, ret)
    }


    fun Series<LocalDate>.mapToStr(): Series<String> {

        return Series(this.name, this.data.mapKeys { "${it.key.year - 2000}-Q${it.key.toQuarter()}" })
    }

    fun extractEps(json: JsonNode): List<Pair<LocalDate, Double>> {
        return json["Earnings"]["History"].map {
            LocalDate.parse(it["reportDate"].textValue()) to it["epsActual"].doubleValue()
        }.sortedBy { it.first }.takeLast(16)
    }


    fun extractFromIncome(json: JsonNode, name: String): Series<LocalDate> {
        val ret = json["Financials"]["Income_Statement"]["quarterly"].associateBy(
            { LocalDate.parse(it["date"].textValue()) },
            { it[name].textValue()?.toDouble() ?: 0.0 })
        return Series(name, ret)
    }


}

fun main() {
    initDatabase()
    transaction {
        CacheService.getCached("some", { "initial".toByteArray() }, 1000)
        Thread.sleep(500)
        val updated = CacheService.getCached("some", { "update0".toByteArray() }, 1000)
        println(String(updated))
        Thread.sleep(600)
        val updated1 = CacheService.getCached("some", { "update1".toByteArray() }, 1000)
        println(String(updated1))

    }
}