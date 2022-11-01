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

    val urlString = "http://localhost:7801"

    private suspend fun postJson(optJson: String): ByteArray {
        val imagePath = client.post<String> {
            url(urlString)
            header("Content-Type", "application/json")
            body = optJson
        }
        return client.get("$urlString/${imagePath}")
    }

    override suspend fun post(options: HOptions, globalOptions: Map<String, *>?, chartType: String): ByteArray {
        val optJson = JsonHelper.toStringPretty(
            HiChartRequest(
                async = true,
                infile = options,
                constr = chartType,
                2,
                globalOptions = globalOptions
            )
        )
        return postJson(optJson)
    }

}