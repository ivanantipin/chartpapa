package com.firelib.techbot.command

import com.firelib.techbot.TABot
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update

class HelpListHandler(val taBot: TABot) : CommandHandler {

    override fun command(): String {
        return "/help"
    }

    override fun category() : CommandCategory{
        return CommandCategory.Other
    }


    override fun handle(cmd: Command, bot: Bot, update: Update) {


        val byCategory = taBot.map.values.groupBy { it.category() }

        fun handlersToStr(lst : List<CommandHandler>) =  lst.joinToString(separator = "\n", transform = {ln-> "${ln.command()} - ${ln.description()}"})


        val shortcut = """
            
         * Simple usage *
            *sber d*
                - so you just need to type  *<ticker> <timeframe>* and it displays all charts for ticker for timeframe,
            
            */sub sber* 
                - subscribe to sber signals
            
            */rm sber* 
                - unsubscribe from sber signals
            
            */list*
                - tickers available
                
             
         """.trimIndent()

        val descr = shortcut + CommandCategory.values().map {cat->

            """*${cat} commands*
${handlersToStr(byCategory[cat]!!)}

""".trimIndent()
        }.joinToString(separator = "\n") + "\n [Поддержка](https://t.me/techBotSupport)"






        bot.sendMessage(
            chatId = update.message!!.chat.id,
            text = descr,
            parseMode = ParseMode.MARKDOWN
        )
    }

    override fun description(): String {
        return "display help"
    }
}