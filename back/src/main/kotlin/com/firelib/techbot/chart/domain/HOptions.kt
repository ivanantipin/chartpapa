package com.firelib.techbot.chart.domain

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.serialization.Serializable


@JsonInclude(JsonInclude.Include.NON_EMPTY)
data class HOptions(
    val title: HTitle? = null,
    val rangeSelector: HRangeSelector =  HRangeSelector(false),
    val chart: HChart? = null,
    @get:JsonProperty("yAxis")
    val yAxis: MutableList<HAxis> = mutableListOf(),
    @get:JsonProperty("xAxis")
    var xAxis: HAxis? = null,
    val series: MutableList<ISeries> = mutableListOf(),
    var navigator : HNavigator = HNavigator(false),
    var scrollbar : HScrollbar = HScrollbar(false),
    val legend : HLegend? = null,
    var annotations : List<HAnnotation> = emptyList(),
    var plotOptions : Map<String,*>? = null,

)


sealed class ISeries{}