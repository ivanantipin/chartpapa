package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions
import firelib.core.misc.JsonHelper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

object ChartService : IChartService {
    val client = HttpClient()

    val urlString = "http://localhost:7801"

    private suspend fun postJson(optJson: String): ByteArray {
        val imagePath = client.post(urlString) {
            contentType(ContentType.Application.Json)
            setBody(optJson)
        }.bodyAsText()
        return client.get("$urlString/${imagePath}").readBytes()
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