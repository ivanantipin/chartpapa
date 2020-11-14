package com.firelib.techbot.chart.domain

import kotlinx.serialization.Serializable


//labels.push({...value.attributes,
//    point: {
//            xAxis: 0,
//            yAxis: 0,
//            x: key,
//            y: value.level
//    },
//    allowOverlap : true
//    //y: -15
//
//})

//base["drawOnTop"] = "" + s.reference.up
//base["backgroundColor"] = if (s.reference.up) "red" else "green"
//base["verticalAlign"] = if (s.reference.up) "bottom" else "top"
//base["distance"] = if (s.reference.up) "10" else "-30"

@Serializable
data class HPoint(
    var xAxis : Int? = null,
    var yAxis : Int? = null,
    var x : Long? = null,
    var y : Double? = null
)

@Serializable
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

@Serializable
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

@Serializable
data class HStyle(
    var fontFamily : String? = null,
    var fontSize : String? = null
)

@Serializable
data class HAnnotation(val labels : List<HLabel>, val shapes : List<HShape>)