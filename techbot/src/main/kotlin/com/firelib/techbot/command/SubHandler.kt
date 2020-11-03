package com.firelib.techbot.command

import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.BotHelper.displaySubscriptions
import com.firelib.techbot.BotHelper.ensureExist
import com.firelib.techbot.BotHelper.parseCommand
import com.firelib.techbot.Subscriptions
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import picocli.CommandLine


class SubHandler : CommandHandler {

    companion object {
        const val command = "/sub"
    }

    override fun commands(): List<String> {
        return listOf(command)
    }


    @CommandLine.Command(name = command, description = ["subscribe for the ticker"])
    class SubCmd : CmdLine {
        @CommandLine.Parameters(description = ["ticker"], index = "0")
        var ticker: String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"], index = "1")
        var timeFrame: TimeFrame = TimeFrame.D

        override fun postConstruct() {
            ticker = ticker.toUpperCase()
        }

    }


    override fun handle(cmd: Command, bot: Bot, update: Update) {
        val subCmd = SubCmd()

        if (!parseCommand(subCmd, cmd.opts, bot, update) ||
            !checkTicker(subCmd.ticker.toUpperCase(), bot, update)
        ) {
            return
        }

        val fromUser = update.message!!.from

        val uid = fromUser!!.id.toInt()

        ensureExist(fromUser)

        updateDatabase("update subscription") {
            if (Subscriptions.select { Subscriptions.user eq uid and (Subscriptions.ticker eq subCmd.ticker) and (Subscriptions.timeframe eq subCmd.timeFrame.name) }
                    .empty()) {
                Subscriptions.insert {
                    it[user] = uid
                    it[ticker] = subCmd.ticker
                    it[timeframe] = subCmd.timeFrame.name
                }
            }
        }.get()

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = displaySubscriptions(uid),
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "Subscribe to breakout events"
    }
}

