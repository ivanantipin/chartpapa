package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

import java.time.LocalDateTime
import java.util.HashMap

data class Label(@get:ApiModelProperty(required = true)
                 val level: Double,
                 @get:ApiModelProperty(required = true)
                 val time: LocalDateTime,
                 @get:ApiModelProperty(required = true)
                 val attributes: Map<String, String>
) {
    fun withAttribute(key: String, obj: String): Label {
        val attrs = HashMap(this.attributes)
        attrs[key] = obj
        return this.copy(attributes = attrs)
    }
}
