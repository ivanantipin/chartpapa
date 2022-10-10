package com.firelib.techbot.chart

import com.firelib.techbot.chart.domain.HOptions

data class HiChartRequest(
    val async: Boolean, val infile: HOptions, val constr: String, val scale: Int,
    var globalOptions: Map<String, *>? = null
)