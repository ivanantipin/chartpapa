package com.firelib.techbot.chart

import com.firelib.techbot.TdLine
import com.firelib.techbot.chart.domain.HOptions
import firelib.core.domain.Ohlc
import firelib.core.misc.JsonHelper
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import org.jetbrains.exposed.sql.logTimeSpent

object ChartService {
    val client = HttpClient()

    data class HiRequest(val async: Boolean, val infile: HOptions, val constr: String, val scale: Int,
                         var globalOptions : Map<String,*>? = null)

    val urlString = "http://localhost:7801"

    private fun postJson(optJson: String): ByteArray {
        return runBlocking {
            val imagePath = client.post<String> {
                url(urlString)
                header("Content-Type", "application/json")
                body = optJson
            }
            val imgUrl = "$urlString/" + imagePath
            //println(optJson)
            println(imgUrl)

            client.get<ByteArray>(imgUrl)
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
        println(optJson)
        return postJson(optJson)
    }

}