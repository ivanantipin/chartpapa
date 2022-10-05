package com.firelib.techbot

import chart.SignalType
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId

data class NotifyGroup(
    val ticker: InstrId,
    val signalType: SignalType,
    val timeFrame: TimeFrame,
    val settings: Map<String, String>
)