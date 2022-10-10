package com.firelib.techbot.command

import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.InstrId

data class Cmd(val handlerName: String, val opts: Map<String, String> = mutableMapOf()) {

    fun instr(staticDataService: InstrumentsService): InstrId {
        return staticDataService.byId(opts["id"]!!)
    }

    fun tf(): TimeFrame {
        return TimeFrame.valueOf(opts["tf"]!!)
    }
}