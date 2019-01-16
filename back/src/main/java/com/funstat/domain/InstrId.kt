package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

data class InstrId(@get:ApiModelProperty(required = true)
                   val id: String = "N/A",
                   @get:ApiModelProperty(required = true)
                   val name: String = "N/A",
                   @get:ApiModelProperty(required = true)
                   val market: String = "N/A",
                   @get:ApiModelProperty(required = true)
                   val code: String = "N/A",
                   @get:ApiModelProperty(required = true)
                   val source: String = "N/A")
