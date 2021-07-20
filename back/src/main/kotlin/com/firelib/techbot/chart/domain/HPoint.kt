package com.firelib.techbot.chart.domain

import com.fasterxml.jackson.annotation.JsonProperty

data class HPoint(
    @get:JsonProperty("xAxis")
    var xAxis : Int? = null,
    @get:JsonProperty("yAxis")
    var yAxis : Int? = null,
    var x : Long? = null,
    var y : Double? = null
)