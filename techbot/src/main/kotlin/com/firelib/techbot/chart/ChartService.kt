package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions
import firelib.core.misc.JsonHelper
import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import org.slf4j.LoggerFactory
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong

object ChartService : IChartService {
    val client = HttpClient()

    val log = LoggerFactory.getLogger(javaClass)

    val list = listOf(
        "http://localhost:8001",
        "http://localhost:8002",
        "http://localhost:8003",
    )

    val cnt = AtomicInteger(0);

    fun nextUrl() : String{
        val idx = cnt.getAndUpdate({ prev ->
            (prev + 1) % list.size
        })
        return list[idx];
    }

    private suspend fun postJson(optJson: String): ByteArray {
        var cnt = 0
        while (true){
            val urlString = nextUrl();
            try {
                val imagePath = client.post(urlString) {
                    contentType(ContentType.Application.Json)
                    setBody(optJson)
                }.bodyAsText()
                return client.get("$urlString/${imagePath}").readBytes()
            }catch (e : Exception){
                cnt++
                if(cnt > 10){
                    throw e
                }
                log.error("failed to retrieve data from url, sleeping for 1 sec", e)
                delay(1000)
            }
        }
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