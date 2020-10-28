package com.firelib.techbot.chart.domain

import kotlinx.serialization.Serializable

@Serializable
data class HLegend(
    val floating: Boolean? = null,
    val layout: String? = null,
    val borderWidth: Int? = null,
    val borderColor: String? = null,
    val align: String? = null,
    val x: Int? = null,
    val verticalAlign: String? = null,
    val y: Int? = null,
    val enabled : Boolean

)