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
        const val descr =  """subscribe for the ticker signals ( demark, levels and trend lines)
            *examples:*
            */sub sber d,w* : _subscribe for sber day and week timeframes_
            */sub sber*  : _subscribe for all timeframes_
        """

    }

    override fun category() : CommandCategory{
        return CommandCategory.Subscriptions
    }


    override fun command(): String {
        return command
    }



    @CommandLine.Command(name = command, description = [SubHandler.descr])
    class SubCmd : CmdLine {
        @CommandLine.Parameters(description = ["ticker"], index = "0")
        var ticker: String = ""

        @CommandLine.Parameters(description = ["timeframe , possible values: \${COMPLETION-CANDIDATES}"],  split = ",")
        var timeFrame: ArrayList<TimeFrame> = ArrayList()

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
            val lst = if(subCmd.timeFrame.isEmpty()) TimeFrame.values().toList() else subCmd.timeFrame
            lst.forEach {tf->
                if (Subscriptions.select { Subscriptions.user eq uid and (Subscriptions.ticker eq subCmd.ticker) and (Subscriptions.timeframe eq tf.name) }
                        .empty()) {
                    Subscriptions.insert {
                        it[user] = uid
                        it[ticker] = subCmd.ticker
                        it[timeframe] = tf.name
                    }
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

