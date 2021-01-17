package com.firelib.techbot.chart.domain

import com.fasterxml.jackson.annotation.JsonInclude
import kotlinx.serialization.Serializable

@Serializable
@JsonInclude(JsonInclude.Include.ALWAYS)
data class HOptions(
    val title: HTitle? = null,
    val rangeSelector: HRangeSelector? = null,
    val chart: HChart? = null,
    val yAxis: MutableList<HAxis> = mutableListOf(),
    var xAxis: HAxis? = null,
    val series: MutableList<HSeries> = mutableListOf(),
    var navigator : HNavigator? = null,
    var scrollbar : HScrollbar? = null,
    val legend : HLegend? = null,
    var annotations : List<HAnnotation> = emptyList()

)