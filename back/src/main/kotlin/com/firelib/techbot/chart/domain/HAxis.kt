package com.firelib.techbot.chart.domain

import java.math.BigDecimal

data class HAxisLabels(val style : HStyle)

data class HAxis(
    val height: String = "100%",
    val lineWidth: Int = 1,
    val gridLineColor: String? = null,
    val tickColor : String? = null,
    val title: HTitle? = null,
    val gapGridLineWidth: Int = 0,
    var left : String? = null,
    var offset : Int? = null,
    var opposite : Boolean? = null,
    var categories : List<String>? = null,
    val tickPositions : List<BigDecimal>? = null,
    val labels: HAxisLabels? = null,

)