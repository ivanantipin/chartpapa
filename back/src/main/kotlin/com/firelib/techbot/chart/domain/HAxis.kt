package com.firelib.techbot.chart.domain

data class HAxis(
    val height: String = "100%",
    val lineWidth: Int = 1,
    val title: HTitle? = null,
    val gapGridLineWidth: Int = 0,
    var left : String? = null,
    var offset : Int? = null,
    var opposite : Boolean? = null,
    var categories : List<String>? = null
)