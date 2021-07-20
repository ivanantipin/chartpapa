package com.firelib.techbot.chart.domain

data class HLabel(
    var point : HPoint? = null,
    var drawOnTop : Boolean? = null,
    var backgroundColor : String? = null,
    var borderColor : String? = null,
    var verticalAlign : String? = null,
    var distance : Int? = null,
    var text : String? = null,
    var shape : String? = null,
    var style : HStyle? = null,
    val allowOverlap : Boolean? = null
)