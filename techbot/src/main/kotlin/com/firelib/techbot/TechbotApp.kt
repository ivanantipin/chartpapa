package com.firelib.techbot

import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.command.*
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.menu.MenuRegistry
import com.firelib.techbot.menu.fromUser
import com.firelib.techbot.menu.userId
import com.firelib.techbot.staticdata.InstrIdDao
import com.firelib.techbot.staticdata.InstrumentRefresher
import com.firelib.techbot.staticdata.InstrumentsService
import com.firelib.techbot.subscriptions.SubscriptionService
import com.firelib.techbot.usernotifier.UsersNotifier
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.dispatcher.callbackQuery
import com.github.kotlintelegrambot.dispatcher.text
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.logging.LogLevel
import firelib.core.store.HistoricalSourceProvider
import firelib.core.store.MdDaoContainer
import firelib.core.store.SingletonsContainer
import firelib.core.store.SourceFactory
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap

open class TechbotApp {
    val services = SingletonsContainer()

    fun start() {
        getUserNotifier().start()
        refresher().start()
        bot().startPolling()
    }

    fun instrumentsService(): InstrumentsService {
        return services.get("staticData", { InstrumentsService(InstrIdDao()) })
    }

    fun subscriptionService(): SubscriptionService {
        return services.get("subscription", { SubscriptionService(instrumentsService()) })
    }

    fun getUserNotifier(): UsersNotifier {
        return services.get("notifier", { UsersNotifier(this.botInterface(), this.ohlcService(), this.instrumentsService(), ChartService) })
    }

    fun refresher(): InstrumentRefresher {
        return services.get("refreshr", {
            InstrumentRefresher(instrumentsService())
        })
    }

    fun subscribeHandler() : SubscribeHandler{
        return services.get("subHandler"){
            SubscribeHandler(subscriptionService(), instrumentsService())
        }
    }

    open fun historicalSourceProvider() : HistoricalSourceProvider{
        return services.get("source provider"){
            SourceFactory()
        }
    }

    open fun signalTypeHandler() : SignalTypeHandler{
        return services.get("signal type handler"){
            SignalTypeHandler()
        }
    }

    open fun removeTfHandler() : RemoveTimeFrameHandler{
        return services.get("remove tf handler"){
            RemoveTimeFrameHandler()
        }
    }



    fun menuRegistry(): MenuRegistry {
        return services.get("menuReg") {
            val menuReg = MenuRegistry(this)
            menuReg.makeMenu()
            menuReg.commandData[SubscribeHandler.name] = subscribeHandler()::handle
            menuReg.commandData[UnsubscribeHandler.name] = UnsubscribeHandler(instrumentsService(), subscriptionService())::handle

            SignalType.values().forEach {
                menuReg.commandData[it.name] = IndicatorCommand(this)::handle
            }

            menuReg.commandData[TimeFrameHandler.name] = TimeFrameHandler()::handle
            menuReg.commandData[SignalTypeHandler.name] = signalTypeHandler()::handle
            menuReg.commandData[RemoveSignalTypeHandler.name] = RemoveSignalTypeHandler()::handle
            menuReg.commandData[RemoveTimeFrameHandler.name] = removeTfHandler()::handle
            menuReg.commandData[LanguageChangeHandler.name] = LanguageChangeHandler()::handle
            menuReg.commandData[FundamentalsCommand.name] = FundamentalsCommand(instrumentsService())::handle

            menuReg.commandData["prune"] = PruneCommandHandler(instrumentsService(), ohlcService())::handle

            menuReg
        }
    }

    val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val lastCommand = ConcurrentHashMap<UserId,MsgEnum>()

    val commandsActions = ConcurrentHashMap<MsgEnum,(txt: String, user: User)->Unit>().apply {
        this[MsgEnum.AddSymbol] = {txt, user->
            ListAddSymbolsHandler(instrumentsService()).handle(txt, bot(), user)
        }
    }

    open fun bot(): Bot {

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
                                scope.launch {
                                    commands[split[0]]!!.handle(split, bot, update)
                                }
                            }
                        }


                        if(!executeFreeTextIfNeeded(text, update)){
                            val msgEnum = MsgEnum.getReverseMap(text)
                            val cmd = if (menuRegistry().menuActions.containsKey(msgEnum) && msgEnum != MsgEnum.MAIN_MENU) msgEnum else MsgEnum.HOME
                            lastCommand.put(update.fromUser().userId(), cmd)
                            scope.launch {
                                menuRegistry().menuActions[cmd]!!(bot, update.fromUser())
                            }
                        }

                    } catch (e: Exception) {
                        mainLogger.error("exception in action ${text}", e)
                    }
                }
                callbackQuery(null) {
                    menuRegistry().processData(this.callbackQuery.data, bot, update)
                }
            }

        }

    }

    private fun executeFreeTextIfNeeded(text : String, update : Update) : Boolean{
        if(MsgEnum.getReverseMap(text) != MsgEnum.None){
            return false
        }
        val lastCmd = lastCommand.getOrDefault(update.fromUser().userId(), MsgEnum.None)
        if (commandsActions[lastCmd] != null) {
            commandsActions[lastCmd]!!(text, update.fromUser())
            return true
        }
        return false
    }

    open fun botInterface(): BotInterface {
        return services.get("botInterface", {
            BotInterfaceImpl(bot())
        })
    }

    fun daoContainer() : MdDaoContainer{
        return services.get("dao container"){
            MdDaoContainer()
        }
    }

    fun ohlcService(): OhlcsService {

        return services.get("ohlcService", {
            val ret = OhlcsService(
                daoContainer(),
                historicalSourceProvider()
            )

            runBlocking {
                subscriptionService().liveInstruments().map {
                    scope.launch {
                        mainLogger.info("launching ohlcs flow for ${it}")
                        ret.initTimeframeIfNeeded(it)
                    }
                }.forEach { it.join() }
            }

            mainLogger.info("flows started")

            subscriptionService().addListener {
                mainLogger.info("launching ohlcs flow for ${it}")
                scope.launch {
                    ret.initTimeframeIfNeeded(it)
                }

            }
            ret.start()
            ret
        })
    }

}