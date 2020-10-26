package com.github.kotlintelegrambot.echo.com.firelib.telbot

import com.firelib.sub.Subscriptions
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.telbot.BotHelper.checkTicker
import firelib.telbot.BotHelper.displaySubscriptions
import firelib.telbot.BotHelper.ensureExist
import firelib.telbot.BotHelper.parseCommand
import firelib.telbot.TimeFrame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import picocli.CommandLine


interface CmdLine{
    fun postConstruct()
}

class SubHandler : CommandHandler {

    companion object{
        const val command = "/sub"
    }

    override fun commands(): List<String> {
        return listOf(command)
    }


    @CommandLine.Command(name = command, description = ["subscribe for the ticker"])
    class SubCmd : CmdLine{
        @CommandLine.Parameters(description = ["ticker"], index = "0")
        var ticker : String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"], index = "1")
        var timeFrame : TimeFrame = TimeFrame.H

        override fun postConstruct() {
            ticker = ticker.toLowerCase()
        }

    }


    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {
        val subCmd = SubCmd()

        if(!parseCommand(subCmd, cmd.opts, bot,update) ||
            !checkTicker(subCmd.ticker.toLowerCase(), bot, update)){
            return
        }


        val fromUser = update.message!!.from

        val uid = fromUser!!.id.toInt()

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            ensureExist(fromUser)

            if (Subscriptions.select {  Subscriptions.user eq uid and (Subscriptions.ticker eq  subCmd.ticker) and (Subscriptions.timeframe eq subCmd.timeFrame.name)}.empty()) {
                Subscriptions.insert {
                    it[user] = uid
                    it[ticker] = subCmd.ticker
                    it[timeframe] = subCmd.timeFrame.name
                }
            }

            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = displaySubscriptions(uid),
                parseMode = ParseMode.MARKDOWN
            )

        }
    }

    override fun description(): String {
        return "Subscribe to breakout events"
    }
}

