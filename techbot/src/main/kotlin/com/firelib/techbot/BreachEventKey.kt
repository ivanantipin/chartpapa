package com.firelib.techbot

import chart.BreachType
import com.firelib.techbot.domain.TimeFrame

data class BreachEventKey(val instrId: String, val tf: TimeFrame, val eventTimeMs: Long, val type: BreachType)