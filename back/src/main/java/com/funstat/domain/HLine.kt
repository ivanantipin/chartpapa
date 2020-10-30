package com.funstat.domain


data class HLine(val start: Long,
                 val end: Long,
                 val level: Double = 0.toDouble(),
                 var dashStyle : String? = null,
                 var color : String? = null
)
