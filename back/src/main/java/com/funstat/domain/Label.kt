package com.funstat.domain



import java.time.LocalDateTime
import java.util.HashMap

data class Label(
                 val level: Double,

                 val time: LocalDateTime,

                 val attributes: Map<String, String>
) {
    fun withAttribute(key: String, obj: String): Label {
        val attrs = HashMap(this.attributes)
        attrs[key] = obj
        return this.copy(attributes = attrs)
    }
}
