package com.funstat.domain

import java.time.LocalDateTime

data class Label(
        val level: Double,
        val time: LocalDateTime,
        val attributes: Map<String, String>
) {
    fun withAttribute(key: String, obj: String): Label {
        return this.copy(attributes = this.attributes + (key to obj))
    }
}
