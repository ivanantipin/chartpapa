package com.firelib.techbot.menu

import com.firelib.techbot.*
import com.firelib.techbot.command.*
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.rsibolinger.RsiBolingerSignals
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class MenuRegistry(val techBotApp: TechbotApp) {
    val menuActions = mutableMapOf<MsgEnum, suspend (Bot, User) -> Unit>()

    val commandData = mutableMapOf<String, suspend (Cmd, Bot, User) -> Unit>()

    companion object {

        val md5Serializer = Md5Serializer()

        fun listButtons(buttons: List<List<BotButton>>, bot: Bot, chatId: ChatId, title: String) {
            val keyboard = InlineKeyboardMarkup.create(
                buttons.map {
                    it.map { but ->
                        InlineKeyboardButton.CallbackData(
                            text = but.name,
                            callbackData = md5Serializer.serialize(but.data)
                        )
                    }
                })
            bot.sendMessage(
                chatId = chatId,
                text = title.ifBlank { "==" },
                replyMarkup = keyboard
            )
        }

        fun listManyButtons(buttons: List<BotButton>, bot: Bot, chatId: ChatId, rowSize: Int, title: String = "") {
            buttons.sortedBy { it.name }.chunked(40).forEach { chunk ->
                listButtons(chunk.chunked(rowSize), bot, chatId, title)
            }
        }

    }

    private fun makeButtons(
        cmd: String,
        chatId: ChatId,
        tf: TimeFrame
    ): List<SimpleButton> {
        val subs = techBotApp.subscriptionService().subscriptions.get(UserId(chatId.getId()))!!
        val bts = subs.values.map { instrId ->
            SimpleButton(
                instrId.code,
                Cmd(cmd, mapOf("id" to instrId.id, "tf" to tf.name))
            )
        }
        return bts
    }


    val callbackScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    fun processData(data: String, bot: Bot, update: Update) {
        callbackScope.launch {
            val command = md5Serializer.deserialize<Cmd>(data)
            Misc.measureAndLogTime("processing command ${command}", {
                try {
                    val handler = commandData.get(command.handlerName)
                    if(handler == null){
                        mainLogger.info("nothing found for command ${command}")
                    }
                    handler?.invoke(command, bot, update.fromUser())
                } catch (e: Exception) {
                    mainLogger.error("failed to process data ${data}", e)
                    bot.sendMessage(update.chatId(), "error happened ${e.message}", parseMode = ParseMode.MARKDOWN)
                }
            })
        }
    }


    fun makeMenu() {
        val root = ParentMenuItem(MsgEnum.HOME).apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            addParentMenu(MsgEnum.HELP) {
                addActionMenu(MsgEnum.SupportChannel, { bot, update ->
                    bot.sendMessage(
                        chatId = update.chatId(),
                        text = "[${MsgEnum.SupportMsg.toLocal(update.langCode())}](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                })
                addActionMenu(MsgEnum.MacdConf, { bot, update ->
                    MacdSignals.displayHelp(bot, update)
                })
                addActionMenu(MsgEnum.RsiBolingerConf, { bot, update ->
                    RsiBolingerSignals.displayHelp(bot, update)
                })

                mainMenu()
            }

        }
        root.register(this)
    }

    private fun ParentMenuItem.mainMenu() {
        addButtonMenu(MsgEnum.MAIN_MENU) {}
    }

    private fun ParentMenuItem.makeFundamentalMenu() {
        addActionMenu(MsgEnum.FUNDAMENTALS, { bot, update ->
            val bts = makeButtons(
                FundamentalsCommand.name,
                update.chatId(),
                TimeFrame.D
            )
            if (bts.isEmpty()) {
                emtyListMsg(bot, update)
            } else {
                listManyButtons(bts, bot, update.chatId(), 4, "Choose company for fundamentals")
            }
        })
    }

    private fun ParentMenuItem.makeInstrumentMenu() {
        addParentMenu(MsgEnum.Instruments) {
            rowSize = 2
            addActionMenu(MsgEnum.AddSymbol){bot, user->
                bot.sendMessage(user.chatId(),
                    text = "Enter text to find ticker",
                    parseMode = ParseMode.MARKDOWN
                )
            }
            addActionMenu(MsgEnum.YourSymbolsOrRemoval, { bot, update ->
                val subs = techBotApp.subscriptionService().subscriptions[UserId(
                    update.chatId().getId()
                )]!!.values.distinct()
                val buttons = subs.map { SimpleButton(it.code, Cmd(UnsubscribeHandler.name, mapOf("id" to it.id))) }
                listManyButtons(buttons,  bot, update.chatId(), 4, MsgEnum.YourSymbolsPressToRemove.toLocal(update.langCode()))
            })
            mainMenu()
        }


        addParentMenu(MsgEnum.SettingsU) {
            rowSize = 2

            addActionMenu(MsgEnum.Language, { bot, update ->
                val buttons =
                    Langs.values().map { SimpleButton(it.name, Cmd(LanguageChangeHandler.name, mapOf("lang" to it.name))) }
                listManyButtons(buttons, bot, update.chatId(), 1, MsgEnum.ChooseLanguage.toLocal(update.langCode()))
            })

            addActionMenu(MsgEnum.Unsubscribe, { bot, update ->
                val buttons = DbHelper.getTimeFrames(update.chatId())
                    .map { SimpleButton(it, Cmd(RemoveTimeFrameHandler.name, mapOf("tf" to it))) }
                listManyButtons(buttons, bot, update.chatId(), 1, MsgEnum.PressTfToUnsubscribe.toLocal(update.langCode()))
            })

            addButtonMenu(MsgEnum.AddTf) {
                title = { lang -> MsgEnum.TfsTitle.toLocal(lang) }
                rowSize = 1
                buttons += TimeFrame.values().map { tf ->
                    SimpleButton(tf.name, Cmd(TimeFrameHandler.name, mapOf("tf" to tf.name)))
                }
            }

            addActionMenu(MsgEnum.UnsubscribeFromSignal, { bot, update ->
                val buttons = DbHelper.getSignalTypes(update.chatId())
                    .map {
                        SimpleButton(
                            it.msgEnum.toLocal(update.langCode()),
                            Cmd(RemoveSignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to it.name))
                        )
                    }
                listManyButtons(buttons, bot, update.chatId(), 1, MsgEnum.YourSignalsOrRemoval.toLocal(update.langCode()))
            })

            addButtonMenu(MsgEnum.AddSignalType) {
                title = { lang -> MsgEnum.PressSignalToSubscribe.toLocal(lang) }
                rowSize = 1
                buttons += SignalType.values().map { signalType ->
                    SimpleButton(
                        signalType.name,
                        Cmd(SignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to signalType.name))
                    )
                }
            }

            addActionMenu(MsgEnum.OtherSettings) { bot, update ->
                SettingsCommand().displaySettings(bot, update.chatId().getId().toLong())
                MacdSignals.displayHelp(bot, update)
            }


            mainMenu()
        }

    }

    private fun ParentMenuItem.makeTechMenu() {
        addParentMenu(MsgEnum.TECH_ANALYSIS) {
            rowSize = 2
            SignalType.values().forEach { stype ->
                addButtonMenu(stype.msgEnum) {
                    title = { lang -> MsgEnum.ChooseTfFor.toLocal(lang) + stype.msgEnum.toLocal(lang) }
                    TimeFrame.values().forEach { tf ->
                        addActionButton(tf.name,  { bot, user ->
                            val bts = makeButtons(stype.name, user.chatId(), tf)
                            mainLogger.info("listing companies for user ${user} buttons size is ${bts.size}")
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, user)
                            } else {
                                listManyButtons(bts, bot, user.chatId(), 4, MsgEnum.Companies.toLocal(user.langCode()))
                            }
                        })
                    }
                }
            }
            addButtonMenu(MsgEnum.MAIN_MENU) {}
        }

        addParentMenu(MsgEnum.OTHER) {
            rowSize = 2

            addActionMenu(MsgEnum.TOP_N, {bot, user->
                TopNExecutor(techBotApp.ohlcService(), techBotApp.instrumentsService()).handle(bot, user)
            })

            addButtonMenu(MsgEnum.MAIN_MENU) {}
        }

    }

    private suspend fun emtyListMsg(bot: Bot, update: User) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions[MsgEnum.SETTINGS]!!(bot, update)
    }

}

