package com.firelib.techbot.chart.domain

import kotlinx.serialization.Serializable

@Serializable
data class HChart(var zoomType: String? = null, var spacing : List<Int>? = null, var margin : List<Int>? = null)