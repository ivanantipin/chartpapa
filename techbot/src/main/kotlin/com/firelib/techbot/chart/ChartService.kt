package com.firelib.techbot.chart

import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.ChartCreator.makeSequentaOpts
import com.firelib.techbot.chart.HiChartCreator.levelBreaches
import com.firelib.techbot.chart.HiChartCreator.makeLevelOptions
import com.firelib.techbot.chart.HiChartCreator.makeTrendLines
import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.chart.domain.SequentaAnnnotations
import com.firelib.techbot.initDatabase
import firelib.core.domain.LevelSignal
import firelib.core.domain.Ohlc
import firelib.core.misc.JsonHelper
import firelib.indicators.SR
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.logTimeSpent
import org.jetbrains.exposed.sql.transactions.transaction

object ChartService {
    val client = HttpClient()

    data class HiRequest(val async: Boolean, val infile: HOptions, val constr: String, val scale: Int,
                         var globalOptions : Map<String,*>? = null)

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
            client.get<ByteArray>("$urlString/" + imagePath)
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

    fun post(options: HOptions, globalOptions: Map<String, *>? = null, chartType : String = "StockChart"): ByteArray {
        val optJson = JsonHelper.toStringPretty(HiRequest(
            async = true,
            infile = options,
            constr = chartType,
            2,
            globalOptions = globalOptions
        ))
        return postJson(optJson)
    }



}

fun main() {
    initDatabase()
    transaction {

    }
}
