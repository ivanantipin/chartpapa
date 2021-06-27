package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId

data class Cmd(val name: String, val opts: Map<String, String> = mutableMapOf()) {

    fun instr() : InstrId {
        return MdService.byId(opts["id"]!!)
    }

    fun tf(): TimeFrame {
        return TimeFrame.valueOf(opts["tf"]!!)
    }
}