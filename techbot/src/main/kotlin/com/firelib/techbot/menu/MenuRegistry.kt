package com.firelib.techbot.menu

import chart.SignalType
import com.firelib.techbot.*
import com.firelib.techbot.command.*
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.macd.RsiBolingerSignals
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
    val menuActions = mutableMapOf<MsgLocalazer, (Bot, Update) -> Unit>()

    val commandData = mutableMapOf<String, (Cmd, Bot, Update) -> Unit>()

    companion object {
        val mainMenu = MsgLocalazer.MAIN_MENU

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
        val root = ParentMenuItem(MsgLocalazer.HOME ).apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            addParentMenu(MsgLocalazer.HELP){
                addActionMenu( MsgLocalazer.SupportChannel, { bot, update ->
                    bot.sendMessage(
                        chatId = update.chatId(),
                        text = "[${MsgLocalazer.SupportMsg.toLocal(update.langCode())}](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                })
                addActionMenu(MsgLocalazer.MacdConf , { bot, update ->
                    MacdSignals.displayHelp(bot, update)
                })
                addActionMenu(MsgLocalazer.RsiBolingerConf , { bot, update ->
                    RsiBolingerSignals.displayHelp(bot, update)
                })

                mainMenu()
            }

        }
        root.register(this)
    }

    private fun ParentMenuItem.mainMenu() {
        addButtonMenu(MsgLocalazer.MAIN_MENU) {}
    }

    private fun ParentMenuItem.makeFundamentalMenu() {
        addActionMenu(MsgLocalazer.FUNDAMENTALS , { bot, update ->
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
        addParentMenu(MsgLocalazer.Instruments) {
            rowSize = 2
            addButtonMenu( MsgLocalazer.AddSymbol) {
                title = {lang-> MsgLocalazer.Choose1stLetterOfCompany.toLocal(lang) }
                this.rowSize = 4
                val staticDataService = techBotApp.staticDataService()
                staticDataService.instrumentByFirstCharacter.keys.forEach { start->
                    this.addButton(start, {langs -> MsgLocalazer.PickCompany.toLocal(langs) }){
                        rowSize = 2
                        buttons +=  staticDataService.instrumentByFirstCharacter.getOrDefault(start, emptyMap()).values.map { code ->
                            SimpleButton("(${code.code}) ${code.name}", Cmd(SubHandler.name, mapOf("id" to code.id)))
                        }
                    }
                }
            }

            addActionMenu(MsgLocalazer.YourSymbolsOrRemoval, { bot, update ->
                val subs = techBotApp.getSubscriptionService().subscriptions[UserId(update.chatId().getId())]!!.values.distinct()

                val buttons = subs.map { SimpleButton(it.code, Cmd(UnsubHandler.name, mapOf("id" to it.id))) }.chunked(4)
                list(buttons, bot, update.chatId(), MsgLocalazer.YourSymbolsPressToRemove.toLocal(update.langCode()))
            })
            mainMenu()
        }


        addParentMenu(MsgLocalazer.SettingsU ) {
            rowSize = 2

            addActionMenu(MsgLocalazer.Language, { bot, update ->
                val buttons = Langs.values().map { SimpleButton(it.name, Cmd(LanguageHandler.name, mapOf("lang" to it.name))) }.chunked(1)
                list(buttons, bot, update.chatId(), MsgLocalazer.ChooseLanguage.toLocal(update.langCode()) )
            })

            addActionMenu(MsgLocalazer.Unsubscribe, { bot, update ->
                val buttons = BotHelper.getTimeFrames(update.chatId())
                    .map { SimpleButton(it, Cmd(RmTfHandler.name, mapOf("tf" to it))) }.chunked(1)
                list(buttons, bot, update.chatId(), MsgLocalazer.PressTfToUnsubscribe.toLocal(update.langCode()) )
            })

            addButtonMenu(MsgLocalazer.AddTf) {
                title = {lang->MsgLocalazer.TfsTitle.toLocal(lang)}
                rowSize = 1
                buttons += TimeFrame.values().map { tf ->
                    SimpleButton(tf.name, Cmd(TfHandler.name, mapOf("tf" to tf.name)))
                }
            }

            addActionMenu(MsgLocalazer.UnsubscribeFromSignal, { bot, update ->
                val buttons = BotHelper.getSignalTypes(update.chatId())
                    .map { SimpleButton(it.msgLocalazer.toLocal(update.langCode()),
                        Cmd(RmSignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to it.name))) }.chunked(1)
                list(buttons, bot, update.chatId(), MsgLocalazer.YourSignalsOrRemoval.toLocal(update.langCode()) )
            })

            addButtonMenu(MsgLocalazer.AddSignalType ) {
                title = {lang->MsgLocalazer.PressSignalToSubscribe.toLocal(lang)}
                rowSize = 1
                buttons += SignalType.values().map { signalType ->
                    SimpleButton(signalType.name, Cmd(SignalTypeHandler.name, mapOf(SignalTypeHandler.SIGNAL_TYPE_ATTRIBUTE to signalType.name)))
                }
            }

            addActionMenu(MsgLocalazer.OtherSettings ) { bot, update->
                SettingsCommand.displaySettings(bot, update.chatId().getId().toLong())
                MacdSignals.displayHelp(bot, update)
            }


            mainMenu()
        }

    }

    private fun ParentMenuItem.makeTechMenu() {
        addParentMenu(MsgLocalazer.TECH_ANALYSIS) {
            rowSize = 2
            SignalType.values().forEach {stype->
                addButtonMenu(stype.msgLocalazer) {
                    title = {lang->  MsgLocalazer.ChooseTfFor.toLocal(lang) + stype.msgLocalazer.toLocal(lang)}
                    TimeFrame.values().forEach { tf ->
                        addActionButton(tf.name, { bot, update ->
                            val bts = makeButtons(stype.settingsName,update.chatId(),tf)
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, update)
                            } else {
                                list(bts.chunked(4), bot, update.chatId(), MsgLocalazer.Companies.toLocal(update.langCode()) )
                            }
                        })
                    }
                }
            }
            addButtonMenu(MsgLocalazer.MAIN_MENU) {}
        }
    }

    private fun emtyListMsg(bot: Bot, update: Update) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions[MsgLocalazer.SETTINGS]!!(bot, update)
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