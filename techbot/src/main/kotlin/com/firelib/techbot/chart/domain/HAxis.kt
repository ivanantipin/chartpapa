package com.firelib.techbot.chart.domain

import kotlinx.serialization.Serializable

@Serializable
data class HAxis(
    val height: String = "100%",
    val lineWidth: Int = 1,
    val title: HTitle? = null,
    val gapGridLineWidth: Int = 0
)