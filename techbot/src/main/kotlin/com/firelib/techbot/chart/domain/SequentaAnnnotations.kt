package com.firelib.techbot.chart.domain

import com.funstat.domain.HLine


data class SequentaAnnnotations(
    val labels: List<HLabel>,
    val shapes: List<HShape>,
                                val lines: List<HLine>
)
