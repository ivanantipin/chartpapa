package com.funstat.domain



import java.time.LocalDateTime
import java.util.HashMap

data class HLine(
                 val start: LocalDateTime,

                 val end: LocalDateTime,

                 val level: Double = 0.toDouble(),

                 val attributes: Map<String, String> = HashMap()
) {

    fun withAttribute(name: String, value: String): HLine {
        val attrs = HashMap(attributes)
        attrs[name] = value
        return copy(attributes = attrs)
    }
}
