package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.command.*
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.persistence.SensitivityConfig
import com.firelib.techbot.persistence.Subscriptions
import com.firelib.techbot.persistence.TimeFrames
import com.firelib.techbot.persistence.Users
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

const val debug_token = "1366338282:AAGb0wrt1IzE_AEj38a9FdUVJWeVzdnZ_HM"

fun main() {

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }

    initDatabase()

    if (System.getenv("TELEGRAM_TOKEN") != null) {

        MdService.updateAll()

        transaction {
            updateSensitivties()
        }
        MdService.startMd()
    }

    val menuReg = MenuRegistry()
    menuReg.makeMenu()
    menuReg.registerHandler(SubHandler())
    menuReg.registerHandler(RmHandler())
    menuReg.registerHandler(DemarkCommand())
    menuReg.registerHandler(FundamentalsCommand())
    menuReg.registerHandler(TrendsCommand())
    menuReg.registerHandler(RmTfHandler())
    menuReg.registerHandler(TfHandler())
    val bot = bot {
        token = System.getenv("TELEGRAM_TOKEN") ?: debug_token
        timeout = 30
        logLevel = LogLevel.Network.Basic
        dispatch {
            text(null) {
                try {
                    val cmd =
                        if (menuReg.menuActions.containsKey(text) && text != MenuRegistry.mainMenu) text else "HOME"
                    menuReg.menuActions[cmd]!!(this.bot, this.update)
                } catch (e: Exception) {
                    mainLogger.error("exception in action ${text}", e)
                }
            }
            callbackQuery(null) {
                try {
                    menuReg.processData(this.callbackQuery.data, bot, update)
                } catch (e: Exception) {
                    mainLogger.error("exception in call back query ${this.callbackQuery?.data}")
                }
            }
        }

    }
    UsersNotifier.start(bot)
    bot.startPolling()
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

        SchemaUtils.create(CommandsLog)
        SchemaUtils.createMissingTablesAndColumns(CommandsLog)

        SchemaUtils.create(TimeFrames)
        SchemaUtils.createMissingTablesAndColumns(TimeFrames)

        SchemaUtils.create(CacheTable)
        SchemaUtils.createMissingTablesAndColumns(CacheTable)
    }
}