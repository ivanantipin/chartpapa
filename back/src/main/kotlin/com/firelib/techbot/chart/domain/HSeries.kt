package com.firelib.techbot.chart.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class HSeries (
    var type: String,
    var name: String,
    var marker: HMarker,
    var data: List<Array<Double>> = mutableListOf(),
    val showInLegend: Boolean,
    val color : String? = null,
    val dashStyle : String? = null,
    val lineWidth: Double? = null
) : ISeries()


data class HSeriesColumn (
    var type: String?,
    var name: String,
    var marker: HMarker,
    var data: List<Double> = mutableListOf(),
    val showInLegend: Boolean,
    val color : String? = null,
    val dashStyle : String? = null,
    val lineWidth: Double? = null,
    @get:JsonProperty("yAxis")
    val yAxis : Int? = null
) : ISeries()



