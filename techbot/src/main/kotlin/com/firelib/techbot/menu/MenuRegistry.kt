package com.firelib.techbot.menu

import chart.SignalType
import com.firelib.techbot.*
import com.firelib.techbot.command.*
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.macd.RsiBolingerSignals
import com.firelib.techbot.staticdata.StaticDataService
import com.firelib.techbot.staticdata.SubscriptionService
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import firelib.core.misc.JsonHelper
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class MenuRegistry(val techBotApp: TechBotApp) {
    val menuActions = mutableMapOf<Msg, (Bot, Update) -> Unit>()

    val commandData = mutableMapOf<String, (Cmd, Bot, Update) -> Unit>()

    companion object {
        val mainMenu = Msg.MAIN_MENU

        fun list(buttons: List<List<IButton>>, bot: Bot, chatId: ChatId, title: String) {
            val keyboard = InlineKeyboardMarkup.create(
                buttons.map {
                    it.map { but ->
                        InlineKeyboardButton.CallbackData(
                            text = but.name,
                            callbackData = JsonHelper.toJsonString(but.data)
                        )
                    }
                })
            bot.sendMessage(
                chatId = chatId,
                text = title.ifBlank { "==" },
                replyMarkup = keyboard
            )
        }

    }

    val pool: ThreadPoolExecutor = Executors.newCachedThreadPool() as ThreadPoolExecutor

    fun processData(data: String, bot: Bot, update: Update) {
        mainLogger.info("pool size is ${pool.poolSize} active count is ${pool.activeCount}")

        pool.execute {
            val command = JsonHelper.fromJson<Cmd>(data)
            measureAndLogTime("processing command ${command}", {
                try {
                    commandData.get(command.handlerName)?.invoke(command, bot, update)
                } catch (e: Exception) {
                    bot.sendMessage(update.chatId(), "error happened ${e.message}", parseMode = ParseMode.MARKDOWN)
                    e.printStackTrace()
                }
            })
        }
    }

    fun listUncat(buttons: List<IButton>, bot: Bot, chatId: ChatId, rowSize: Int, title: String = "") {
        buttons.chunked(40).forEach { chunk ->
            list(chunk.chunked(rowSize), bot, chatId, title)
        }
    }

    fun makeMenu() {
        val root = ParentMenuItem(Msg.HOME ).apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            addParentMenu(Msg.HELP){
                addActionMenu( Msg.SupportChannel, { bot, update ->
                    bot.sendMessage(
                        chatId = update.chatId(),
                        text = "[${Msg.SupportMsg.toLocal(update.langCode())}](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                })
                addActionMenu(Msg.MacdConf , { bot, update ->
                    MacdSignals.displayHelp(bot, update)
                })
                addActionMenu(Msg.RsiBolingerConf , { bot, update ->
                    RsiBolingerSignals.displayHelp(bot, update)
                })

                mainMenu()
            }

        }
        root.register(this)
    }

    private fun ParentMenuItem.mainMenu() {
        addButtonMenu(Msg.MAIN_MENU) {}
    }

    private fun ParentMenuItem.makeFundamentalMenu() {
        addActionMenu(Msg.FUNDAMENTALS , { bot, update ->
            val bts = makeButtons(
                FundamentalsCommand.name,
                update.chatId(),
                TimeFrame.D
            )
            if (bts.isEmpty()) {
                emtyListMsg(bot, update)
            } else {
                list(
                    bts.chunked(4),
                    bot,
                    update.chatId(),
                    "Выберите компанию для показа фундаментальных данных"
                )
            }
        })
    }

    private fun ParentMenuItem.makeInstrumentMenu() {
        addParentMenu(Msg.Instruments) {
            rowSize = 2
            addButtonMenu( Msg.AddSymbol) {
                title = {lang-> Msg.Choose1stLetterOfCompany.toLocal(lang) }
                this.rowSize = 4
                val staticDataService = techBotApp.staticDataService()
                staticDataService.instrumentByFirstCharacter.keys.forEach { start->
                    this.addButton(start, {langs -> Msg.PickCompany.toLocal(langs) }){
                        rowSize = 2
                        buttons +=  staticDataService.instrumentByFirstCharacter.getOrDefault(start, emptyMap()).values.map { code ->
                            SimpleButton("(${code.code}) ${code.name}", Cmd(SubHandler.name, mapOf("id" to code.id)))
                        }
                    }
                }
            }

            addActionMenu(Msg.YourSymbolsOrRemoval, { bot, update ->
                val subs = techBotApp.getSubscriptionService().subscriptions[UserId(update.chatId().getId())]!!.values.distinct()

                val buttons = subs.map { SimpleButton(it.code, Cmd(UnsubHandler.name, mapOf("id" to it.id))) }.chunked(4)
                list(buttons, bot, update.chatId(), Msg.YourSymbolsPressToRemove.toLocal(update.langCode()))
            })
            mainMenu()
        }


        addParentMenu(Msg.SettingsU ) {
            rowSize = 2

            addActionMenu(Msg.Language, { bot, update ->
                val buttons = Langs.values().map { SimpleButton(it.name, Cmd(LanguageHandler.name, mapOf("lang" to it.name))) }.chunked(1)
                list(buttons, bot, update.chatId(), Msg.ChooseLanguage.toLocal(update.langCode()) )
            })

            addActionMenu(Msg.Unsubscribe, { bot, update ->
                val buttons = BotHelper.getTimeFrames(update.chatId())
                    .map { SimpleButton(it, Cmd(RmTfHandler.name, mapOf("tf" to it))) }.chunked(1)
                list(buttons, bot, update.chatId(), Msg.PressTfToUnsubscribe.toLocal(update.langCode()) )
            })

            addButtonMenu(Msg.AddTf) {
                title = {lang->Msg.TfsTitle.toLocal(lang)}
                rowSize = 1
                buttons += TimeFrame.values().map { tf ->
                    SimpleButton(tf.name, Cmd(TfHandler.name, mapOf("tf" to tf.name)))
                }
            }

            addActionMenu(Msg.UnsubscribeFromSignal, { bot, update ->
                val buttons = BotHelper.getSignalTypes(update.chatId())
                    .map { SimpleButton(it.msg.toLocal(update.langCode()),
                        Cmd(RmSignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to it.name))) }.chunked(1)
                list(buttons, bot, update.chatId(), Msg.YourSignalsOrRemoval.toLocal(update.langCode()) )
            })

            addButtonMenu(Msg.AddSignalType ) {
                title = {lang->Msg.PressSignalToSubscribe.toLocal(lang)}
                rowSize = 1
                buttons += SignalType.values().map { signalType ->
                    SimpleButton(signalType.name, Cmd(SignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to signalType.name)))
                }
            }

            addActionMenu(Msg.OtherSettings ) {bot, update->
                SettingsCommand.displaySettings(bot, update.chatId().getId().toLong())
                MacdSignals.displayHelp(bot, update)
            }


            mainMenu()
        }

    }

    private fun ParentMenuItem.makeTechMenu() {
        addParentMenu(Msg.TECH_ANALYSIS) {
            rowSize = 2
            SignalType.values().forEach {stype->
                addButtonMenu(stype.msg) {
                    title = {lang->  Msg.ChooseTfFor.toLocal(lang) + stype.msg.toLocal(lang)}
                    TimeFrame.values().forEach { tf ->
                        addActionButton(tf.name, { bot, update ->
                            val bts = makeButtons(stype.settingsName,update.chatId(),tf)
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, update)
                            } else {
                                list(bts.chunked(4), bot, update.chatId(), Msg.Companies.toLocal(update.langCode()) )
                            }
                        })
                    }
                }
            }
            addButtonMenu(Msg.MAIN_MENU) {}
        }
    }

    private fun emtyListMsg(bot: Bot, update: Update) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions[Msg.SETTINGS]!!(bot, update)
    }

    private fun makeButtons(
        cmd: String,
        chatId: ChatId,
        tf: TimeFrame
    ): List<SimpleButton> {
        val subs = techBotApp.getSubscriptionService().subscriptions.get(UserId(chatId.getId()))!!
        val bts = subs.values.map { instrId ->
            SimpleButton(
                instrId.code,
                Cmd(cmd, mapOf("id" to instrId.id, "tf" to tf.name))
            )
        }
        return bts
    }

}