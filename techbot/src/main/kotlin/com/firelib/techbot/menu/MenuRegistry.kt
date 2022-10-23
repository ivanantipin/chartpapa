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
import firelib.core.misc.JsonHelper
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class MenuRegistry(val techBotApp: TechbotApp) {
    val menuActions = mutableMapOf<MsgLocalizer, (Bot, User) -> Unit>()

    val commandData = mutableMapOf<String, (Cmd, Bot, User) -> Unit>()

    companion object {

        fun listButtons(buttons: List<List<BotButton>>, bot: Bot, chatId: ChatId, title: String) {
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

        fun listManyButtons(buttons: List<BotButton>, bot: Bot, chatId: ChatId, rowSize: Int, title: String = "") {
            buttons.chunked(40).forEach { chunk ->
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


    val pool: ThreadPoolExecutor = Executors.newCachedThreadPool({
        Thread(it).apply {
            isDaemon = true
        }
    }) as ThreadPoolExecutor

    fun processData(data: String, bot: Bot, update: Update) {
        mainLogger.info("pool size is ${pool.poolSize} active count is ${pool.activeCount}")

        pool.execute {
            val command = JsonHelper.fromJson<Cmd>(data)
            Misc.measureAndLogTime("processing command ${command}", {
                        try {
                            commandData.get(command.handlerName)?.invoke(command, bot, update.fromUser())
                        } catch (e: Exception) {
                            bot.sendMessage(update.chatId(), "error happened ${e.message}", parseMode = ParseMode.MARKDOWN)
                            e.printStackTrace()
                        }
                    })
        }
    }


    fun makeMenu() {
        val root = ParentMenuItem(MsgLocalizer.HOME).apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            addParentMenu(MsgLocalizer.HELP) {
                addActionMenu(MsgLocalizer.SupportChannel, { bot, update ->
                    bot.sendMessage(
                        chatId = update.chatId(),
                        text = "[${MsgLocalizer.SupportMsg.toLocal(update.langCode())}](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                })
                addActionMenu(MsgLocalizer.MacdConf, { bot, update ->
                    MacdSignals.displayHelp(bot, update)
                })
                addActionMenu(MsgLocalizer.RsiBolingerConf, { bot, update ->
                    RsiBolingerSignals.displayHelp(bot, update)
                })

                mainMenu()
            }

        }
        root.register(this)
    }

    private fun ParentMenuItem.mainMenu() {
        addButtonMenu(MsgLocalizer.MAIN_MENU) {}
    }

    private fun ParentMenuItem.makeFundamentalMenu() {
        addActionMenu(MsgLocalizer.FUNDAMENTALS, { bot, update ->
            val bts = makeButtons(
                FundamentalsCommand.name,
                update.chatId(),
                TimeFrame.D
            )
            if (bts.isEmpty()) {
                emtyListMsg(bot, update)
            } else {
                listButtons(
                    bts.chunked(4),
                    bot,
                    update.chatId(),
                    "Выберите компанию для показа фундаментальных данных"
                )
            }
        })
    }

    private fun ParentMenuItem.makeInstrumentMenu() {
        addParentMenu(MsgLocalizer.Instruments) {
            rowSize = 2
            addButtonMenu(MsgLocalizer.AddSymbol) {
                title = { lang -> MsgLocalizer.Choose1stLetterOfCompany.toLocal(lang) }
                this.rowSize = 4
                val staticDataService = techBotApp.instrumentsService()
                staticDataService.instrumentByFirstCharacter.keys.forEach { start ->
                    this.addButton(start, { langs -> MsgLocalizer.PickCompany.toLocal(langs) }) {
                        rowSize = 2
                        buttons += staticDataService.instrumentByFirstCharacter.getOrDefault(
                            start,
                            emptyMap()
                        ).values.map { code ->
                            SimpleButton("(${code.code}) ${code.name}", Cmd(SubscribeHandler.name, mapOf("id" to code.id)))
                        }
                    }
                }
            }

            addActionMenu(MsgLocalizer.YourSymbolsOrRemoval, { bot, update ->
                val subs = techBotApp.subscriptionService().subscriptions[UserId(
                    update.chatId().getId()
                )]!!.values.distinct()

                val buttons =
                    subs.map { SimpleButton(it.code, Cmd(UnsubscribeHandler.name, mapOf("id" to it.id))) }.chunked(4)
                listButtons(buttons, bot, update.chatId(), MsgLocalizer.YourSymbolsPressToRemove.toLocal(update.langCode()))
            })
            mainMenu()
        }


        addParentMenu(MsgLocalizer.SettingsU) {
            rowSize = 2

            addActionMenu(MsgLocalizer.Language, { bot, update ->
                val buttons =
                    Langs.values().map { SimpleButton(it.name, Cmd(LanguageChangeHandler.name, mapOf("lang" to it.name))) }
                        .chunked(1)
                listButtons(buttons, bot, update.chatId(), MsgLocalizer.ChooseLanguage.toLocal(update.langCode()))
            })

            addActionMenu(MsgLocalizer.Unsubscribe, { bot, update ->
                val buttons = DbHelper.getTimeFrames(update.chatId())
                    .map { SimpleButton(it, Cmd(RemoveTimeFrameHandler.name, mapOf("tf" to it))) }.chunked(1)
                listButtons(buttons, bot, update.chatId(), MsgLocalizer.PressTfToUnsubscribe.toLocal(update.langCode()))
            })

            addButtonMenu(MsgLocalizer.AddTf) {
                title = { lang -> MsgLocalizer.TfsTitle.toLocal(lang) }
                rowSize = 1
                buttons += TimeFrame.values().map { tf ->
                    SimpleButton(tf.name, Cmd(TimeFrameHandler.name, mapOf("tf" to tf.name)))
                }
            }

            addActionMenu(MsgLocalizer.UnsubscribeFromSignal, { bot, update ->
                val buttons = DbHelper.getSignalTypes(update.chatId())
                    .map {
                        SimpleButton(
                            it.msgLocalizer.toLocal(update.langCode()),
                            Cmd(RemoveSignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to it.name))
                        )
                    }.chunked(1)
                listButtons(buttons, bot, update.chatId(), MsgLocalizer.YourSignalsOrRemoval.toLocal(update.langCode()))
            })

            addButtonMenu(MsgLocalizer.AddSignalType) {
                title = { lang -> MsgLocalizer.PressSignalToSubscribe.toLocal(lang) }
                rowSize = 1
                buttons += SignalType.values().map { signalType ->
                    SimpleButton(
                        signalType.name,
                        Cmd(SignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to signalType.name))
                    )
                }
            }

            addActionMenu(MsgLocalizer.OtherSettings) { bot, update ->
                SettingsCommand().displaySettings(bot, update.chatId().getId().toLong())
                MacdSignals.displayHelp(bot, update)
            }


            mainMenu()
        }

    }

    private fun ParentMenuItem.makeTechMenu() {
        addParentMenu(MsgLocalizer.TECH_ANALYSIS) {
            rowSize = 2
            SignalType.values().forEach { stype ->
                addButtonMenu(stype.msgLocalizer) {
                    title = { lang -> MsgLocalizer.ChooseTfFor.toLocal(lang) + stype.msgLocalizer.toLocal(lang) }
                    TimeFrame.values().forEach { tf ->
                        addActionButton(tf.name, { bot, user ->
                            val bts = makeButtons(stype.name, user.chatId(), tf)
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, user)
                            } else {
                                listButtons(
                                    bts.chunked(4),
                                    bot,
                                    user.chatId(),
                                    MsgLocalizer.Companies.toLocal(user.langCode())
                                )
                            }
                        })
                    }
                }
            }
            addButtonMenu(MsgLocalizer.MAIN_MENU) {}
        }
    }

    private fun emtyListMsg(bot: Bot, update: User) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions[MsgLocalizer.SETTINGS]!!(bot, update)
    }


}