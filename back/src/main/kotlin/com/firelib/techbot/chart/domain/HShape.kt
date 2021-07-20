package com.firelib.techbot.chart.domain

data class HShape(
    var fill : String? = null,
    var stroke : String? = null,
    var strokeWidth : Int? = null,
    var dashStyle : String? = null,
    var type : String? = null,
    var markerEnd : String? = null,
    var markerStart : String? = null,
    var points : List<HPoint>? = null,
    var height : Int? = null

)