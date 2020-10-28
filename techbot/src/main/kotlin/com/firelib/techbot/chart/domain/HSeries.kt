package com.firelib.techbot.chart.domain

import kotlinx.serialization.Serializable

@Serializable
data class HSeries(
    var type: String,
    var name: String,
    var marker: HMarker,
    var data: List<Array<Double>> = mutableListOf(),
    val showInLegend: Boolean,
    val color : String? = null,
    val dashStyle : String? = null,
    val lineWidth: Double? = null
)