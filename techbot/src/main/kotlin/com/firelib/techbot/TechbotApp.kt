package com.firelib.techbot

import com.firelib.techbot.command.*
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.subscriptions.Subscriptions
import com.firelib.techbot.staticdata.*
import com.firelib.techbot.subscriptions.SubscriptionService
import com.firelib.techbot.usernotifier.UsersNotifier
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.store.MdStorageImpl
import firelib.core.store.SingletonsContainer
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.Executors

class TechbotApp {
    val services = SingletonsContainer()

    fun start() {
        getUserNotifier().start()
        refresher().start()
        bot().startPolling()
        startSubsMigration()
    }

    fun startSubsMigration() {
        Thread {
            while (true) {
                val subscriptionService = subscriptionService()
                val staticDataService = instrumentsService()
                if (staticDataService.id2inst.size > 0) {
                    mainLogger.info("start migration , static data size is ${staticDataService.id2inst.size}")
                    DbHelper.updateDatabase("migration") {
                        Subscriptions.selectAll().forEach {
                            val instr =
                                staticDataService.instrByCodeAndMarket.get(it[Subscriptions.ticker] to it[Subscriptions.market])
                            if (instr != null) {
                                val userId = it[Subscriptions.user]
                                subscriptionService.addSubscription(UserId(userId), instr)
                                Subscriptions.deleteWhere { (Subscriptions.ticker eq instr.code) and (Subscriptions.user eq userId) and (Subscriptions.market eq instr.market) }
                            }
                        }
                    }.get()
                    break
                } else {
                    Thread.sleep(1000)
                }

            }
        }.start()

    }

    fun instrumentsService(): InstrumentsService {
        return services.get("staticData", { InstrumentsService(InstrIdDao()) })
    }

    fun subscriptionService(): SubscriptionService {
        return services.get("subscription", { SubscriptionService(instrumentsService()) })
    }

    fun getUserNotifier(): UsersNotifier {
        return services.get("notifier", { UsersNotifier(this) })
    }

    fun refresher(): InstrumentRefresher {
        return services.get("refreshr", {
            InstrumentRefresher(instrumentsService())
        })
    }

    fun mdStorage(): MdStorageImpl {
        return services.get("mdStorage", {
            MdStorageImpl()
        })
    }

    fun menuRegistry(): MenuRegistry {
        return services.get("menuReg") {
            val menuReg = MenuRegistry(this)
            menuReg.makeMenu()
            menuReg.commandData[SubscribeHandler.name] = SubscribeHandler(subscriptionService(), instrumentsService())::handle
            menuReg.commandData[UnsubscribeHandler.name] = UnsubscribeHandler(instrumentsService(), subscriptionService())::handle

            SignalType.values().forEach {
                menuReg.commandData[it.name] = IndicatorCommand(this)::handle
            }

            menuReg.commandData[TimeFrameHandler.name] = TimeFrameHandler()::handle
            menuReg.commandData[SignalTypeHandler.name] = SignalTypeHandler()::handle
            menuReg.commandData[RemoveSignalTypeHandler.name] = RemoveSignalTypeHandler()::handle
            menuReg.commandData[RemoveTimeFrameHandler.name] = RemoveTimeFrameHandler()::handle
            menuReg.commandData[LanguageChangeHandler.name] = LanguageChangeHandler()::handle
            menuReg.commandData[FundamentalsCommand.name] = FundamentalsCommand(instrumentsService())::handle

            menuReg.commandData["prune"] = PruneCommandHandler(instrumentsService(), ohlcService())::handle

            menuReg
        }
    }

    fun bot(): Bot {

        val commands = mapOf(
            "/set" to SettingsCommand(),
            "/prune" to PruneSearchCommand(instrumentsService(), subscriptionService())
        )

        return bot {
            token = ConfigParameters.TELEGRAM_TOKEN.get()!!
            timeout = 30
            logLevel = LogLevel.Error
            dispatch {
                text(null) {
                    try {
                        val split = text.split(" ", "\t")
                        if (split.isNotEmpty()) {
                            if(commands.containsKey(split[0])){
                                commands[split[0]]!!.handle(split, this.bot, this.update)
                            }
                        }
                        val msgLocalizer = MsgLocalizer.getReverseMap(text)
                        val cmd = if (menuRegistry().menuActions.containsKey(msgLocalizer) && msgLocalizer != MsgLocalizer.MAIN_MENU) msgLocalizer else MsgLocalizer.HOME
                        menuRegistry().menuActions[cmd]!!(this.bot, this.update)
                    } catch (e: Exception) {
                        mainLogger.error("exception in action ${text}", e)
                    }
                }
                callbackQuery(null) {
                    try {
                        menuRegistry().processData(this.callbackQuery.data, bot, update)
                    } catch (e: Exception) {
                        mainLogger.error("exception in call back query ${this.callbackQuery?.data}")
                    }
                }
            }

        }

    }

    fun botInterface(): BotInterface {
        return services.get("botInterface", {
            BotInterfaceImpl(bot())
        })
    }

    fun ohlcService(): OhlcsService {

        return services.get("ohlcService", {
            val mdStorage = mdStorage()
            val ret = OhlcsService(
                { src, interval -> mdStorage.daos.getDao(src, interval) },
                { src -> mdStorage.sources[src] })

            val pool = Executors.newCachedThreadPool()
            subscriptionService().liveInstruments().map {
                pool.submit({
                    mainLogger.info("launching ohlcs flow for ${it}")
                    ret.initTimeframeIfNeeded(it)
                })
            }.forEach { it.get() }
            pool.shutdown()

            mainLogger.info("flows started")

            subscriptionService().addListener {
                mainLogger.info("launching ohlcs flow for ${it}")
                ret.initTimeframeIfNeeded(it)
            }
            ret.start()
            ret
        })
    }

}