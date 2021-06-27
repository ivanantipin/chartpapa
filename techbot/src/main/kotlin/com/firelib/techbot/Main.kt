package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.firelib.techbot.command.*
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.store.GlobalConstants
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.FileOutputStream


const val debug_token = "1366338282:AAGb0wrt1IzE_AEj38a9FdUVJWeVzdnZ_HM"

fun main() {
    initDatabase()

    //MdService.updateAll()

    transaction {
        updateSensitivties()
        UpdateLevelsSensitivities.updateLevelSenses()
    }

    //MdService.startMd()

    val menuReg = MenuReg()
    menuReg.makeMenu()

    menuReg.registerHandler(SubHandler())

    menuReg.registerHandler(RmHandler())

    menuReg.registerHandler(DemarkCommand())

    menuReg.registerHandler(LevelsCommand())

    menuReg.registerHandler(TrendsCommand())

    menuReg.registerHandler(RmTfHandler())

    menuReg.registerHandler(TfHandler())

    val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?: debug_token
        timeout = 30
        logLevel = LogLevel.Network.Body
        dispatch {
            text(null) {
                var cmd = if (menuReg.menuActions.containsKey(text) && text != MenuReg.mainMenu) text else "HOME"
                menuReg.menuActions[cmd]!!(this.bot, this.update)
            }
            callbackQuery(null) {
                menuReg.processData(this.callbackQuery.data, bot, update)
            }
        }


    }
    UsersNotifier.start(bot)
    bot.startPolling()
}


fun makeBot(taBot: TABot): Bot {
    val bt = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?: debug_token
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            text(null) {
                taBot.handle(text, bot, update)
            }
        }
    }
    return bt
}


fun initDatabase() {
    Database.connect(
        "jdbc:sqlite:${GlobalConstants.metaDb.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )
    transaction {

        addLogger(StdOutSqlLogger)
        SchemaUtils.create(Users)
        SchemaUtils.createMissingTablesAndColumns(Users)

        SchemaUtils.create(SensitivityConfig)
        SchemaUtils.createMissingTablesAndColumns(SensitivityConfig)

        SchemaUtils.create(Subscriptions)
        SchemaUtils.createMissingTablesAndColumns(Subscriptions)

        SchemaUtils.create(BreachEvents)
        SchemaUtils.createMissingTablesAndColumns(BreachEvents)

        SchemaUtils.create(LevelSensitivityConfig)
        SchemaUtils.createMissingTablesAndColumns(LevelSensitivityConfig)

        SchemaUtils.create(CommandsLog)
        SchemaUtils.createMissingTablesAndColumns(CommandsLog)

        SchemaUtils.create(TimeFrames)
        SchemaUtils.createMissingTablesAndColumns(TimeFrames)


    }
}

fun saveFile(bytes: ByteArray, fileName: String) {
    FileOutputStream(fileName).use {
        it.write(bytes)
    }
}