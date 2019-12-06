package com.funstat.domain

import java.math.BigDecimal


data class InstrId(val id: String = "N/A",
        val name: String = "N/A",
        val market: String = "N/A",
        val code: String = "N/A",
        val source: String = "N/A",
        val minPriceIncr : BigDecimal = BigDecimal(0.1),
                   val lot : Int = 1
)
