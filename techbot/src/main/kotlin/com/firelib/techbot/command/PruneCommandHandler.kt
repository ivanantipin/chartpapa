package com.firelib.techbot.command

import com.firelib.techbot.mainLogger
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.staticdata.InstrumentsService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update
import firelib.core.domain.Interval
import firelib.core.store.MdStorageImpl
import java.time.Instant

class PruneCommandHandler(val storageImpl: MdStorageImpl, val staticDataService: InstrumentsService) : CommandHandler {
    override fun command(): String {
        return "prune"
    }

    override fun handle(cmd: Cmd, bot: Bot, update: Update) {
        val instr = cmd.instr(staticDataService)
        val fromUser = update.fromUser()
        val secInDay = 24*3600
        storageImpl.deleteSince(instr, Interval.Min10, Instant.now().minusSeconds(7*secInDay.toLong()))
        mainLogger.info("pruned instrument ${instr} by ${fromUser}")
    }
}