package firelib.telbot

import com.firelib.sub.Subscriptions
import com.firelib.sub.Users
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.com.firelib.telbot.CmdLine
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import picocli.CommandLine
import java.io.ByteArrayOutputStream
import java.io.PrintStream

object BotHelper {
    fun displaySubscriptions(uid: Int): String {
        val resp = Subscriptions.select {
            Subscriptions.user eq uid
        }.map {
            "${it[Subscriptions.ticker]} : ${it[Subscriptions.timeframe]}"
        }.sorted().joinToString(separator = "\n")
        return resp
    }


    fun ensureExist(user : User){
        if (Users.select { Users.id eq user.id!!.toInt() }.count() == 0L) {
            Users.insert {
                it[name] = user.firstName
            }
        }
    }

    fun getUsage(cmd : Any) : String{
        val outStr = ByteArrayOutputStream()
        CommandLine.usage(cmd, PrintStream(outStr))
        return String(outStr.toByteArray())
    }

    fun parseCommand(subCmd : CmdLine, args : List<String>, bot : Bot, update : Update) : Boolean{
        try {
            CommandLine(subCmd).setCaseInsensitiveEnumValuesAllowed(true). parseArgs(*args.toTypedArray())
            subCmd.postConstruct()
        }catch (e : Exception){
            e.printStackTrace()
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = getUsage(subCmd),
                parseMode = ParseMode.MARKDOWN
            )
            return false
        }
        return true
    }

    fun checkTicker(ticker : String, bot : Bot, update : Update): Boolean {
        if (SymbolsDao.available().find { it.code.equals(ticker, true) } == null) {
            bot.sendMessage(
                chatId = update.message!!.chat.id,
                text = "invalid ticker ${ticker}",
                parseMode = ParseMode.MARKDOWN
            )
            return false
        }
        return true
    }
}