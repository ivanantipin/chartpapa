package com.firelib.techbot

import chart.SignalType
import com.firelib.techbot.command.*
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.persistence.Subscriptions
import com.firelib.techbot.staticdata.*
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

class TechBotApp {
    val services = SingletonsContainer()

    fun start(){
        getUserNotifier().start()
        refresher().start()
        bot().startPolling()
        startSubsMigration()
    }

    fun startSubsMigration(){
        Thread{
            while (true){
                val subscriptionService = getSubscriptionService()
                val staticDataService = staticDataService()
                if(staticDataService.id2inst.size > 0){
                    mainLogger.info("start migration , static data size is ${staticDataService.id2inst.size}")
                    updateDatabase("migration"){
                        Subscriptions.selectAll().forEach {
                            val instr = staticDataService.instrByCodeAndMarket.get(it[Subscriptions.ticker] to it[Subscriptions.market])
                            if(instr != null){
                                val userId = it[Subscriptions.user]
                                subscriptionService.addSubscription(UserId(userId), instr)
                                Subscriptions.deleteWhere { (Subscriptions.ticker  eq instr.code) and (Subscriptions.user eq userId) and (Subscriptions.market eq instr.market)}
                            }
                        }
                    }.get()
                    break
                }else{
                    Thread.sleep(1000)
                }

            }
        }.start()

    }


    fun staticDataService() : InstrumentsService{
        return services.get("staticData", {InstrumentsService(InstrIdDao())})
    }

    fun getSubscriptionService() : SubscriptionService{
        return services.get("subscription", { SubscriptionService(staticDataService()) })
    }

    fun getUserNotifier() : UsersNotifier{
        return services.get("notifier", {UsersNotifier(this)})
    }

    fun refresher() : InstrumentRefresher{
        return services.get("refreshr",{
            InstrumentRefresher(staticDataService())
        })
    }

    fun mdStorage() : MdStorageImpl{
        return services.get("mdStorage",{
            MdStorageImpl()
        })
    }

    fun menuRegistry() : MenuRegistry{
        return services.get("menuReg"){
            val menuReg = MenuRegistry(this)
            menuReg.makeMenu()
            menuReg.commandData[SubHandler.name] = SubHandler(getSubscriptionService(), staticDataService())::handle
            menuReg.commandData[UnsubHandler.name] = UnsubHandler(staticDataService(), getSubscriptionService())::handle

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
        return bot {
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
                        val cmd = if (menuRegistry().menuActions.containsKey(msg) && msg != Msg.MAIN_MENU) msg else Msg.HOME
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

    fun botInterface() : BotInterface{
        return services.get("botInterface", {
            BotInterfaceImpl(bot())
        })
    }

    fun ohlcService() : OhlcsService{

        return services.get("ohlcService", {
            val mdStorage = mdStorage()
            val ret = OhlcsService(
                { src, interval -> mdStorage.daos.getDao(src, interval) },
                { src -> mdStorage.sources[src] })

            getSubscriptionService().liveInstruments().forEach {
                mainLogger.info("launching ohlcs flow for ${it}")
                ret.launchFlowIfNeeded(it)
            }
            mainLogger.info("waiting while initial load to complete")
            ret.baseFlows.values.map { it.completed }.forEach { it.get() }
            mainLogger.info("flows started")

            getSubscriptionService().addListener {
                mainLogger.info("launching ohlcs flow for ${it}")
                ret.launchFlowIfNeeded(it)
            }
            ret
        })
    }

}