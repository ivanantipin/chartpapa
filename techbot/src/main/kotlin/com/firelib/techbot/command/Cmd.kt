package com.firelib.techbot.command

import com.firelib.techbot.MdService
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