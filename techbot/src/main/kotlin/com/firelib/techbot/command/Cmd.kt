package com.firelib.techbot.command

import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.InstrId

data class Cmd(val handlerName: String, val opts: Map<String, String> = mutableMapOf()) {

    fun instr(staticDataService: InstrumentsService): InstrId {
        return staticDataService.byId(instrId())
    }

    fun instrId() : String{
        return opts["id"]!!
    }

    fun tf(): List<TimeFrame> {
        val ret = TimeFrame.valueOf(opts["tf"]!!)
        if(ret == TimeFrame.All){
            return TimeFrame.values().filter { it != TimeFrame.All }
        }
        return listOf(ret)
    }
}