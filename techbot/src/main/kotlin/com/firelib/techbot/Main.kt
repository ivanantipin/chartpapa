package com.firelib.techbot

import chart.SignalType
import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.command.*
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.persistence.*
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.store.GlobalConstants
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }
    initDatabase()
    ConfigService.initSystemVars()
    val app = TechBotApp()

    updateSensitivties(app.getSubscriptionService(), app.ohlcService())
}


fun initDatabase() {
    Database.connect(
        "jdbc:sqlite:${GlobalConstants.metaDb.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )

    fun populateSignalTypes(){
        updateDatabase("populate signal types"){
            val allSignals: Map<UserId, List<SignalType>> = BotHelper.getSignalTypes()
            Users.selectAll().forEach { userRow->
                val siggi = allSignals.getOrDefault(UserId(userRow[Users.userId]), emptyList())
                if(siggi.isEmpty()){
                    mainLogger.info("populating for user ${userRow[Users.name]}  ${userRow[Users.familyName]}" )
                    SignalTypes.insert {
                        it[user] = userRow[Users.userId]
                        it[signalType] = SignalType.TREND_LINE.name
                    }
                    SignalTypes.insert {
                        it[user] = userRow[Users.userId]
                        it[signalType] = SignalType.DEMARK.name
                    }
                    SignalTypes.insert {
                        it[user] = userRow[Users.userId]
                        it[signalType] = SignalType.RSI_BOLINGER.name
                    }

                }
            }
        }
    }

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

        SchemaUtils.create(SignalTypes)
        SchemaUtils.createMissingTablesAndColumns(SignalTypes)

        SchemaUtils.create(CacheTable)
        SchemaUtils.createMissingTablesAndColumns(CacheTable)

        SchemaUtils.create(Settings)
        SchemaUtils.createMissingTablesAndColumns(Settings)

        SchemaUtils.create(ConfigService)
        SchemaUtils.createMissingTablesAndColumns(ConfigService)

    }
    populateSignalTypes()


}