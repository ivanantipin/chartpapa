package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions
import firelib.core.misc.JsonHelper
import io.ktor.client.*
import io.ktor.client.request.*
import kotlinx.coroutines.runBlocking
import java.util.concurrent.Callable
import java.util.concurrent.Executors

object ChartService : IChartService {
    val client = HttpClient()

    val executor = Executors.newSingleThreadExecutor()

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

    override fun post(options: HOptions, globalOptions: Map<String, *>?, chartType: String): ByteArray {
        val optJson = JsonHelper.toStringPretty(
            HiChartRequest(
                async = true,
                infile = options,
                constr = chartType,
                2,
                globalOptions = globalOptions
            )
        )
        //println(optJson)
        return postJson(optJson)
    }

}