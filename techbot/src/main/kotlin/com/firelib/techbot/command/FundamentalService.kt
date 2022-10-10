package com.firelib.techbot.command

import com.fasterxml.jackson.databind.JsonNode
import com.firelib.techbot.chart.Series
import com.firelib.techbot.chart.toSortedList
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.readJson
import firelib.core.store.MdStorageImpl
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.ZoneOffset
import java.util.*
import kotlin.math.absoluteValue
import kotlin.math.sign

object FundamentalService {

    fun <T> List<T>.mapTrailing(extr: (T) -> Double, n: Int): List<Double> {
        return this.mapIndexed({ idx, el ->
            subList(maxOf(idx - n + 1, 0), idx + 1).sumOf { extr(it) }
        })
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
        return mergeAndSort(
            listOf(
                extractFromIncome(json, "operatingIncome"),
                extractFromIncome(json, "costOfRevenue"),
                extractFromIncome(json, "totalOperatingExpenses")
            )
        ).map { it.mapToStr() }
    }

    fun debtToFcF(instrId: InstrId): List<Series<String>> {
        val json = fetchRest(instrId.code).readJson()
        val cashFlow = extractFromCashFlow(json, "freeCashFlow")
        val debt = extractFromBs(json, "netDebt")
        val merged: List<Series<LocalDate>> = mergeAndSort(listOf(cashFlow, debt))
        val annualFcf = merged[0].toSortedList().mapTrailing({ it.second }, 4)

        val ndata = merged[1].toSortedList().mapIndexed({ idx, oo ->
            val value = oo.second / annualFcf[idx]
            oo.first to cap(value, 20.0)
        }).toMap()
        return listOf(Series("fcfToDebt", ndata).mapToStr(), merged[1].mapToStr())
    }

    fun cap(value: Double, cap: Double = 30.0): Double {
        if (value.absoluteValue > cap) {
            return cap * value.sign
        }
        return value
    }

    /*
    returns 1st ev, 2nd ev-to-ebidta
     */
    fun ev2Ebitda(instrId: InstrId, mdDao: MdStorageImpl): List<Series<String>> {
        val json = fetchRest(instrId.code).readJson()
        val debt = extractFromBs(json, "netDebt")

        val shares = json["SharesStats"]["SharesOutstanding"].longValue()

        val data = debt.data.mapValues {
            val ms = it.key.atStartOfDay().toEpochSecond(ZoneOffset.UTC) * 1000
            mdDao.queryPoint(instrId, Interval.Min10, ms)?.close.let { it?.times(shares) }
        }.filterValues { it != null }.mapValues { it.value!! }

        val cap = Series("cap", data)

        val ebitda = extractFromIncome(json, "ebitda")

        val merged: List<List<Pair<LocalDate, Double>>> =
            mergeAndSort(listOf(debt, cap, ebitda)).map { it.toSortedList() }

        val trailingYear = merged[2].mapTrailing({ it.second }, 4)

        val ndata = merged[0].mapIndexed { idx, p ->
            val dbt = merged[0][idx].second
            val cp = merged[1][idx].second
            val evEbitda = (dbt + cp) / trailingYear[idx]
            val evEbitdaCapped = cap(evEbitda)
            Triple(p.first, evEbitdaCapped, dbt + cp)
        }

        return listOf(Series("ev", ndata.associateBy({ it.first }, { it.third })).mapToStr(),
            Series("evToEbitda", ndata.associateBy({ it.first }, { it.second })).mapToStr()
        )
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

    fun <T : Comparable<T>> mergeAndSort(list: List<Series<T>>, lastN: Int = 16): List<Series<T>> {
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
