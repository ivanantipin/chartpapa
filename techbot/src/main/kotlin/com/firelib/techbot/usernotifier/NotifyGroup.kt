package com.firelib.techbot.usernotifier

import com.firelib.techbot.SignalType
import com.firelib.techbot.domain.TimeFrame

data class NotifyGroup(
    val instrumentId: String,
    val signalType: SignalType,
    val timeFrame: TimeFrame,
    val settings: Map<String, String>
)