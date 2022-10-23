package com.firelib.techbot.breachevent

import com.firelib.techbot.SignalType
import com.firelib.techbot.domain.TimeFrame
import java.time.Instant

data class BreachEventKey(val instrId: String, val tf: TimeFrame, val eventTimeMs: Long, val type: SignalType){

    override fun toString(): String {
        return "BreachEventKey(instrId='$instrId', tf=$tf, eventTimeMs=${Instant.ofEpochMilli(eventTimeMs)}, type=$type)"
    }
}

