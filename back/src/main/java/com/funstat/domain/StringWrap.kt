package com.funstat.domain

import io.swagger.annotations.ApiModelProperty

data class StringWrap(@get:ApiModelProperty(required = true) val value : String)