package com.firelib.techbot.chart

import chart.at
import com.firelib.techbot.BotHelper
import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.ChartCreator.makeSequentaOpts
import com.firelib.techbot.chart.HiChartCreator.levelBreaches
import com.firelib.techbot.chart.HiChartCreator.makeLevelOptions
import com.firelib.techbot.chart.HiChartCreator.makeTrendLines
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.chart.domain.SequentaAnnnotations
import com.firelib.techbot.initDatabase
import firelib.core.domain.*
import firelib.indicators.SR
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.logTimeSpent
import org.jetbrains.exposed.sql.transactions.transaction

object ChartService {
    val client = HttpClient()

    @Serializable
    data class HiRequest(val async: Boolean, val infile: HOptions, val constr: String, val scale: Int)

    val urlString = "http://localhost:7801"

    fun drawSequenta(ann: SequentaAnnnotations, hours: List<Ohlc>, title: String): ByteArray {
        return logTimeSpent("draw sequenta hicharts server") {
            post(makeSequentaOpts(ann, hours, title))
        }
    }

    private fun postJson(optJson: String): ByteArray {
        return runBlocking {
            val imagePath = client.post<String> {
                url(urlString)
                header("Content-Type", "application/json")
                body = optJson
            }
            val url = "$urlString/" + imagePath
            println(url)
            client.get<ByteArray>(url)
        }
    }

    fun drawLevels(
        lines: List<SR>,
        hours: List<Ohlc>,
        title: String
    ): ByteArray {
        return logTimeSpent("draw levels hicharts server") {
            post(makeLevelOptions(hours, title, lines))
        }
    }

    fun drawLevelsBreaches(
        signals: List<LevelSignal>,
        hours: List<Ohlc>,
        title: String
    ): ByteArray {
        return logTimeSpent("draw levels hicharts server") {
            post(levelBreaches(hours, title, signals))
        }
    }

    fun drawLines(
        lines: List<TdLine>,
        hours: List<Ohlc>,
        title: String
    ): ByteArray {
        return logTimeSpent("draw lines hicharts server") {
            post(makeTrendLines(hours, title, lines))
        }
    }

    private fun post(options: HOptions): ByteArray {
        val optJson = Json { prettyPrint = true }.encodeToString(
            HiRequest(
                async = true,
                infile = options,
                constr = "StockChart",
                2
            )
        )
        return postJson(optJson)
    }

}

fun main() {
    initDatabase()
    transaction {
        val ohs = BotHelper.getOhlcsForTf(InstrId.fromCodeAndExch("plzl", "1"), Interval.Day)
        val sr = SR(ohs[0].endTime, ohs[20].endTime, ohs[20].high)
        val time = ohs.at(-5).endTime.toEpochMilli()
        val sigi = LevelSignal(Side.Sell, time, sr)
        ChartService.drawLevelsBreaches(listOf(sigi), ohs, "some")
    }
}
