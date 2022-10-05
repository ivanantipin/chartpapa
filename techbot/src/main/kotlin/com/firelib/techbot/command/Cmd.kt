package com.firelib.techbot.command

import com.firelib.techbot.staticdata.StaticDataService
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId

data class Cmd(val handlerName: String, val opts: Map<String, String> = mutableMapOf()) {

    fun instr(staticDataService: StaticDataService) : InstrId {
        return staticDataService.byId(opts["id"]!!)
    }

    fun tf(): TimeFrame {
        return TimeFrame.valueOf(opts["tf"]!!)
    }
}