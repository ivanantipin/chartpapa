package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

import java.time.LocalDateTime
import java.util.HashMap

data class HLine(@get:ApiModelProperty(required = true)
                 val start: LocalDateTime,
                 @get:ApiModelProperty(required = true)
                 val end: LocalDateTime,
                 @get:ApiModelProperty(required = true)
                 val level: Double = 0.toDouble(),
                 @get:ApiModelProperty(required = true)
                 val attributes: Map<String, String> = HashMap()
) {

    fun withAttribute(name: String, value: String): HLine {
        val attrs = HashMap(attributes)
        attrs[name] = value
        return copy(attributes = attrs)
    }
}
