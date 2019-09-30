package com.funstat.domain


import java.time.LocalDateTime

data class HLine(val start: LocalDateTime,
                 val end: LocalDateTime,
                 val level: Double = 0.toDouble(),
                 val attributes: Map<String, String> = emptyMap()
) {

    fun withAttribute(name: String, value: String): HLine {
        return copy(attributes = attributes + (name to value))
    }
}
