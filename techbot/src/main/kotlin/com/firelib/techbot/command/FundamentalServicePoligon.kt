package com.firelib.techbot.command

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node.ArrayNode
import com.firelib.techbot.ConfigParameters
import com.firelib.techbot.chart.Series
import com.firelib.techbot.chart.SeriesUX
import com.firelib.techbot.chart.toSortedList
import com.firelib.techbot.command.FundamentalService.mergeAndSort
import com.firelib.techbot.command.FundamentalService.toQuarter
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.mapper
import firelib.core.misc.readJson
import firelib.core.store.MdStorageImpl
import org.springframework.web.client.RestTemplate
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.math.absoluteValue
import kotlin.math.sign

object FundamentalServicePoligon {

    fun fetchCursor(url: String, keyAttribute: String): ByteArray {
        val template = RestTemplate()
        var json = template.getForEntity(url + "&" + keyAttribute, String::class.java).body.readJson()
        val arrayNode = json["results"] as ArrayNode
        while (json["next_url"] != null) {
            json = template.getForEntity(
                json["next_url"].textValue() + "&${keyAttribute}",
                String::class.java
            ).body.readJson()
            arrayNode.addAll((json["results"] as ArrayNode))
        }
        return mapper.writeValueAsBytes(arrayNode)
    }

    data class CompanyInfo(val shares: Long)

    val apiKey = "apiKey=${ConfigParameters.POLYGON_TOKEN.get()}"

    fun getCompanyInfo(instrId: InstrId): CompanyInfo {

        val cached = CacheService.getCached("/companyInfo/${instrId.code}", {
            val template = RestTemplate()
            template.getForEntity(
                "https://api.polygon.io/vX/reference/tickers/${instrId.code}?${apiKey}",
                String::class.java
            ).body.toByteArray()
        }, Interval.Min60.durationMs)
        return CompanyInfo(String(cached).readJson()["results"]!!["outstanding_shares"]!!.numberValue().toLong())
    }

    fun fetchRest(ticker: String): String {
        val url = "https://api.polygon.io/vX/reference/financials?ticker=${ticker}&timeframe=quarterly&limit=24"
        return String(fetchCursor(url, apiKey))
    }

    fun fetchRestCached(ticker: String): ByteArray {
        return CacheService.getCached("/ff/${ticker}", {
            fetchRest(ticker).toByteArray()
        }, Interval.Min60.durationMs)
    }

    fun <T> List<T>.mapTrailing(extr: (T) -> Double, n: Int): List<Double> {
        return this.mapIndexed({ idx, el ->
            subList(maxOf(idx - n + 1, 0), idx + 1).sumOf { extr(it) }
        })
    }

    fun getFromIncome(instrId: InstrId, list: List<String>): List<Series<String>> {
        val cached = fetchRestCached(instrId.code)
        return mergeAndSort(
            list.map {
                extractFromIncome(it, String(cached).readJson())
            }
        ).map { it.mapToStr() }
    }

    fun getFromBalanceSheet(instrId: InstrId, list: List<String>): List<Series<String>> {
        val cached = fetchRestCached(instrId.code)
        return mergeAndSort(
            list.map {
                extractFromBalanceSheet(it, String(cached).readJson())
            }
        ).map { it.mapToStr() }
    }

    fun getFromCashFlow(instrId: InstrId, list: List<String>): List<Series<String>> {
        val cached = fetchRestCached(instrId.code)
        return mergeAndSort(
            list.map {
                extractFromCashFlow(it, String(cached).readJson())
            }
        ).map { it.mapToStr() }
    }

    fun extractFromIncome(name: String, json: JsonNode): Series<LocalDate> {
        return extractSeries(json, "financials", "income_statement", name, "value").copy(name = name)
    }

    fun extractFromBalanceSheet(name: String, json: JsonNode): Series<LocalDate> {
        return extractSeries(json, "financials", "balance_sheet", name, "value").copy(name = name)
    }

    fun extractFromCashFlow(name: String, json: JsonNode): Series<LocalDate> {
        return extractSeries(json, "financials", "cash_flow_statement", name, "value").copy(name = name)
    }

    fun cap(value: Double, cap: Double = 30.0): Double {
        if (value.absoluteValue > cap) {
            return cap * value.sign
        }
        return value
    }

    fun ev2Ebitda(instrId: InstrId, mdDao: MdStorageImpl): List<SeriesUX> {
        val json = fetchRest(instrId.code).readJson()
        val ev = getEv(json, instrId, mdDao)
        val ebitda = extractFromIncome("revenues", json)
        val debt = extractFromBalanceSheet("liabilities", json)
        val merged = mergeAndSort(listOf(ev, ebitda, debt))
        return listOf(
            SeriesUX(merged[0].mapToStr(), "blue", 0, "line", false),
            SeriesUX(merged[1].mapToStr(), "green", 0, "line", false),
            SeriesUX(merged[2].mapToStr(), "red", 0, "line", false),
        )
    }

    private fun getEv(
        json: JsonNode,
        instrId: InstrId,
        mdDao: MdStorageImpl
    ): Series<LocalDate> {
        val debt = extractFromBalanceSheet("liabilities", json)

        val shares = getCompanyInfo(instrId).shares

        val capitalization = debt.data.mapValues {
            val ms = it.key.atStartOfDay().toInstant(ZoneOffset.UTC).toEpochMilli()
            mdDao.queryPoint(instrId, Interval.Min10, ms)?.close.let { it?.times(shares) }
        }.filterValues { it != null }.mapValues { it.value!! }

        val cap = Series("cap", capitalization)

        val merged = mergeAndSort(listOf(debt, cap)).map { it.toSortedList() }

        val ndata = merged[0].mapIndexed { idx, p ->
            val dbt = merged[0][idx].second
            val cp = merged[1][idx].second
            p.first to (dbt + cp)
        }
        return Series("ev", ndata.associateBy({ it.first }, { it.second }))
    }

    fun JsonNode.readNode(vararg path: String): JsonNode? {
        return path.fold(this, { nd: JsonNode?,
                                 name ->
            if (nd == null) null else nd[name]
        })
    }

    private fun extractSeries(
        data: JsonNode,
        vararg path: String
    ): Series<LocalDate> {
        val ret = (data as ArrayNode).associateBy(
            { LocalDate.parse(it["end_date"].textValue()) },
            {
                val ret = it.readNode(*path)
                ret?.numberValue()?.toDouble() ?: 0.0
            }
        ).mapValues { it.value }
        return Series("", ret)
    }

    fun Series<LocalDate>.mapToStr(): Series<String> {
        return Series(this.name, this.data.mapKeys { "${it.key.year - 2000}-Q${it.key.toQuarter()}" })
    }
}