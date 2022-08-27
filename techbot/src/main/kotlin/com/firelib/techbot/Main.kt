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

const val debug_token = "5406386828:AAGxrq7n-wyDzois-NSZe8-Ye3vIfyNj0-o"

fun main() {

    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }

    initDatabase()

    transaction {
        ConfigService.selectAll().forEach {
            System.setProperty(it.get(ConfigService.name), it.get(ConfigService.value))
        }
    }

    MdService.updateAll()

    updateSensitivties()

    MdService.startMd()

    val menuReg = MenuRegistry()
    menuReg.makeMenu()
    menuReg.commandData[SubHandler.name] = SubHandler()::handle
    menuReg.commandData[UnsubHandler.name] = UnsubHandler()::handle

    SignalType.values().forEach {
        menuReg.commandData[it.settingsName] = IndicatorCommand()::handle
    }

    menuReg.commandData[TfHandler.name] = TfHandler()::handle
    menuReg.commandData[SignalTypeHandler.name] = SignalTypeHandler()::handle
    menuReg.commandData[RmSignalTypeHandler.name] = RmSignalTypeHandler()::handle
    menuReg.commandData[RmTfHandler.name] = RmTfHandler()::handle
    menuReg.commandData[LanguageHandler.name] = LanguageHandler()::handle
    menuReg.commandData[FundamentalsCommand.name] = FundamentalsCommand()::handle

    val bot = bot {
        token = ConfigParameters.TELEGRAM_TOKEN.get()  ?: debug_token
        timeout = 30
        logLevel = LogLevel.Error
        dispatch {
            text(null) {
                try {
                    val split = text.split(" ", "\t")
                    if(split.isNotEmpty() && split[0] == "/set"){
                        SettingsCommand().handle(split, this.bot, this.update)
                    }

                    val msg = Msg.getReverseMap(text)

                    val cmd = if (menuReg.menuActions.containsKey(msg) && msg != Msg.MAIN_MENU) msg else Msg.HOME
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