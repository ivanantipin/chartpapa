package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions
import firelib.core.misc.JsonHelper
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object ChartService {
    val client = HttpClient()

    val executor = Executors.newSingleThreadExecutor()

    data class HiRequest(val async: Boolean, val infile: HOptions, val constr: String, val scale: Int,
                         var globalOptions : Map<String,*>? = null)

    val urlString = "http://localhost:7801"

    private fun postJson(optJson: String): ByteArray {
        return executor.submit(Callable {
            runBlocking {
                val imagePath = client.post<String> {
                    url(urlString)
                    header("Content-Type", "application/json")
                    body = optJson
                }
                val imgUrl = "$urlString/" + imagePath
                //println(optJson)
                //println(imgUrl)

                client.get<ByteArray>(imgUrl)
            }
        }).get()
    }

    fun post(options: HOptions, globalOptions: Map<String, *>? = null, chartType : String = "StockChart"): ByteArray {
        val optJson = JsonHelper.toStringPretty(HiRequest(
            async = true,
            infile = options,
            constr = chartType,
            2,
            globalOptions = globalOptions
        ))
        //println(optJson)
        return postJson(optJson)
    }

}