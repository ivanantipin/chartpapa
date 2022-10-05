package com.firelib.techbot

import chart.SignalType
import com.firelib.techbot.command.*
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.staticdata.InstrIdDao
import com.firelib.techbot.staticdata.OhlcsService
import com.firelib.techbot.staticdata.StaticDataService
import com.firelib.techbot.staticdata.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.store.MdStorageImpl
import firelib.core.store.SingletonsContainer
import java.awt.Menu

class TechBotApp {
    val services = SingletonsContainer()

    fun start(){
        getUserNotifier().start()
        bot().startPolling()
    }

    fun staticDataService() : StaticDataService{
        return services.get("staticData", {StaticDataService(InstrIdDao())})
    }

    fun getSubscriptionService() : SubscriptionService{
        return services.get("subscription", { SubscriptionService(staticDataService()) })
    }

    fun getUserNotifier() : UsersNotifier{
        return services.get("notifier", {UsersNotifier(this)})
    }

    fun mdStorage() : MdStorageImpl{
        return services.get("mdStorage",{
            MdStorageImpl()
        })
    }

    fun menuReg() : MenuRegistry{
        return services.get("menuReg"){
            val menuReg = MenuRegistry(this)
            menuReg.makeMenu()
            menuReg.commandData[SubHandler.name] = SubHandler(getSubscriptionService(), staticDataService())::handle
            menuReg.commandData[UnsubHandler.name] = UnsubHandler(staticDataService())::handle

            SignalType.values().forEach {
                menuReg.commandData[it.settingsName] = IndicatorCommand(this)::handle
            }

            menuReg.commandData[TfHandler.name] = TfHandler()::handle
            menuReg.commandData[SignalTypeHandler.name] = SignalTypeHandler()::handle
            menuReg.commandData[RmSignalTypeHandler.name] = RmSignalTypeHandler()::handle
            menuReg.commandData[RmTfHandler.name] = RmTfHandler()::handle
            menuReg.commandData[LanguageHandler.name] = LanguageHandler()::handle
            menuReg.commandData[FundamentalsCommand.name] = FundamentalsCommand(staticDataService())::handle
            menuReg
        }
    }

    fun bot() : Bot{
        return com.github.kotlintelegrambot.bot {
            token = ConfigParameters.TELEGRAM_TOKEN.get()!!
            timeout = 30
            logLevel = LogLevel.Error
            dispatch {
                text(null) {
                    try {
                        val split = text.split(" ", "\t")
                        if (split.isNotEmpty() && split[0] == "/set") {
                            SettingsCommand().handle(split, this.bot, this.update)
                        }

                        val msg = Msg.getReverseMap(text)

                        val cmd = if (menuReg().menuActions.containsKey(msg) && msg != Msg.MAIN_MENU) msg else Msg.HOME
                        menuReg().menuActions[cmd]!!(this.bot, this.update)
                    } catch (e: Exception) {
                        mainLogger.error("exception in action ${text}", e)
                    }
                }
                callbackQuery(null) {
                    try {
                        menuReg().processData(this.callbackQuery.data, bot, update)
                    } catch (e: Exception) {
                        mainLogger.error("exception in call back query ${this.callbackQuery?.data}")
                    }
                }
            }

        }

    }

    fun botInterface() : BotInterface{
        return services.get("botInterface", {
            BotInterfaceImpl(bot())
        })
    }

    fun ohlcService() : OhlcsService{

        return services.get("ohlcService", {
            val mdStorage = mdStorage()
            OhlcsService(
                {src, interval-> mdStorage.daos.getDao(src, interval)},
                {src->mdStorage.sources[src]},
                subscriptionService = getSubscriptionService()
            )
        })
    }

}