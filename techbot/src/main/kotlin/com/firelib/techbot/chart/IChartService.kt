package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions

interface IChartService {
    suspend fun post(options: HOptions, globalOptions: Map<String, *>? = null, chartType: String = "StockChart"): ByteArray
}