package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

data class Annotations(@get:ApiModelProperty(required = true)
                       val labels: List<Label>,
                       @get:ApiModelProperty(required = true)
                       val lines: List<HLine>
)
