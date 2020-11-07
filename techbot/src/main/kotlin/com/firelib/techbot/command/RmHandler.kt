package com.firelib.techbot.command

import com.firelib.techbot.BotHelper
import com.firelib.techbot.BotHelper.checkTicker
import com.firelib.techbot.BotHelper.displaySubscriptions
import com.firelib.techbot.BotHelper.ensureExist
import com.firelib.techbot.Subscriptions
import com.firelib.techbot.updateDatabase
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import picocli.CommandLine

class RmHandler : CommandHandler {

    override fun category() : CommandCategory{
        return CommandCategory.Subscriptions
    }


    companion object{
        const val command = "/rm"

        @CommandLine.Command(name = command, description = ["unsubscribe from ticker all timeframes"])
        class RmCmd : CmdLine{
            @CommandLine.Parameters(description = ["ticker"], )
            var ticker : String = ""
            override fun postConstruct() {
                ticker = ticker.toUpperCase()
            }
        }
    }

    override fun command(): String {
        return command
    }

    override fun handle(cmd: Command, bot: Bot, update: Update) {
        val subCmd = RmCmd()

        if(!BotHelper.parseCommand(subCmd, cmd.opts, bot,update) ||
            !checkTicker(subCmd.ticker, bot, update)){
            return
        }

        val fromUser = update.message!!.from

        val uid = fromUser!!.id.toInt()

        updateDatabase("delete user", {
            Subscriptions.deleteWhere { Subscriptions.user eq uid and (Subscriptions.ticker eq subCmd.ticker) }
        }).get()

        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = displaySubscriptions(uid),
            parseMode = ParseMode.MARKDOWN
        )
    }


    override fun description(): String {
        return "Remove subscription for ticker"
    }
}