package com.github.kotlintelegrambot.echo.firelib.telbot

import com.firelib.sub.Subscriptions
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.*
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import firelib.telbot.BotHelper
import firelib.telbot.BotHelper.checkTicker
import firelib.telbot.BotHelper.displaySubscriptions
import firelib.telbot.BotHelper.ensureExist
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.transactions.transaction
import picocli.CommandLine

class RmHandler : CommandHandler {

    companion object{
        const val command = "/rm"

        @CommandLine.Command(name = command, description = ["unsubscribe from ticker all timeframes"])
        class RmCmd : CmdLine{
            @CommandLine.Parameters(description = ["ticker"], )
            var ticker : String = ""
            override fun postConstruct() {
                ticker = ticker.toLowerCase()
            }
        }
    }

    override fun commands(): List<String> {
        return listOf(command)
    }

    override suspend fun handle(cmd: Command, bot: Bot, update: Update) {

        val subCmd = RmCmd()

        if(!BotHelper.parseCommand(subCmd, cmd.opts, bot,update) ||
            !checkTicker(subCmd.ticker, bot, update)){
            return
        }

        val fromUser = update.message!!.from

        val uid = fromUser!!.id.toInt()

        transaction {
            // print sql to std-out
            addLogger(StdOutSqlLogger)

            ensureExist(fromUser)

            Subscriptions.deleteWhere { Subscriptions.user eq uid and (Subscriptions.ticker eq subCmd.ticker) }

            val resp = displaySubscriptions(uid)

            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = resp,
                parseMode = ParseMode.MARKDOWN
            )

        }
    }


    override fun description(): String {
        return "Remove tickers from subscriptions"
    }
}