package com.firelib.techbot

import com.firelib.techbot.command.CommandHandler
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.chatId
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import firelib.core.misc.JsonHelper
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class MenuRegistry {
    val menuActions = mutableMapOf<String, (Bot, Update) -> Unit>()

    val commandData = mutableMapOf<String, (Bot, Update) -> Unit>()

    val handlers = mutableMapOf<String, CommandHandler>()

    fun registerHandler(handler: CommandHandler) {
        handlers[handler.command()] = handler
    }

    companion object {
        val mainMenu = "Главное меню"
    }

    private fun reg(ret: MenuItem) {
        if (ret.action != null) {
            menuActions[ret.name] = ret.action!!
        } else if (ret.children.isNotEmpty()) {

            menuActions[ret.name] = { bot, update ->
                val sm = ret.children.map { KeyboardButton(it.name) }.chunked(ret.rowSize)
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
                bot.sendMessage(
                    chatId = update.chatId(),
                    text = ret.name,
                    replyMarkup = keyboardMarkup
                )
            }
        } else {
            menuActions[ret.name] = { bot, update ->
                list(ret.buttons.chunked(ret.rowSize), bot, update.chatId(), ret.title)
            }
        }

        ret.buttons.forEach { reg(it) }
        ret.children.forEach { reg(it) }
    }

    private fun reg(inlineButton: InlineButton) {
        require(
            !commandData.containsKey(inlineButton.data.name),
            { "already registered button ${inlineButton.data.name}" })
        if (inlineButton.action != null) {
            commandData[inlineButton.data.name] = inlineButton.action!!
        } else if (inlineButton.buttons.isNotEmpty()) {
            commandData[inlineButton.data.name] = { bot, update ->
                listUncat(inlineButton.buttons, bot, update.chatId(), inlineButton.rowSize)
            }
        }
        inlineButton.buttons.forEach { reg(it) }
    }

    val pool: ThreadPoolExecutor = Executors.newCachedThreadPool() as ThreadPoolExecutor

    fun processData(data: String, bot: Bot, update: Update) {
        mainLogger.info("pool size is ${pool.poolSize} active count is ${pool.activeCount}")

        pool.execute {
            val command = JsonHelper.fromJson<Cmd>(data)
            measureAndLogTime("processing command ${command}") {
                try {
                    handlers.get(command.name)?.handle(command, bot, update)
                    commandData.get(command.name)?.invoke(bot, update)
                } catch (e: Exception) {
                    bot.sendMessage(update.chatId(), "error happened ${e.message}", parseMode = ParseMode.MARKDOWN)
                    e.printStackTrace()
                }
            }
        }
    }

    fun list(buttons: List<List<InlineButton>>, bot: Bot, chatId: ChatId, title: String) {
        val keyboard = InlineKeyboardMarkup.create(
            buttons.map {
                it.map { but ->
                    InlineKeyboardButton.CallbackData(text = but.name, callbackData = JsonHelper.toJsonString(but.data))
                }
            })
        bot.sendMessage(
            chatId = chatId,
            text = title,
            replyMarkup = keyboard
        )
    }

    fun listUncat(buttons: List<InlineButton>, bot: Bot, chatId: ChatId, rowSize: Int) {
        buttons.chunked(40).forEach { chunk ->
            chunk.groupBy { it.title }.forEach {
                val ttl = if (it.key.isBlank()) "NA" else it.key;
                list(it.value.chunked(rowSize), bot, chatId, ttl)
            }
        }
    }

    fun makeMenu() {
        val root = MenuItem("HOME").apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            menuItem("Помощь") {
                action = { bot, update ->
                    bot.sendMessage(
                        chatId = update.chatId(),
                        text = "[Поддержка](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                }
            }
        }
        reg(root)
    }

    private fun MenuItem.makeFundamentalMenu() {
        menuItem("Фундаментальные данные") {
            rowSize = 1
            action = { bot, update ->
                val bts = makeButtons(
                    "fund",
                    update.chatId(),
                    TimeFrame.D,
                    "Выберите тикер"
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
            }
            menuItem("Главное меню") {}
        }
    }

    private fun MenuItem.makeInstrumentMenu() {
        menuItem("Инструменты/Подписки") {
            rowSize = 1
            parentInlButton("Добавить символ") {
                rowSize = 4

                MdService.instrByStart.keys.forEach { start ->
                    subInlButton(start, "Выберите начальную букву тикера:") {
                        rowSize = 2
                        MdService.instrByStart.getOrDefault(start, emptyList()).forEach { code ->
                            subInlButton(
                                "(${code.code}) ${code.name}",
                                Cmd("sub", mapOf("id" to code.id)),
                                "Выберите компанию для добавления:"
                            ) {}
                        }
                    }
                }
            }

            parentInlButton("Ваши символы / Удаление") {
                action = { bot, update ->
                    val buttons = BotHelper.getSubscriptions(update.chatId().getId().toInt()).distinct()
                        .map { InlineButton(it.code, Cmd("unsub", mapOf("id" to it.id)), "") }.chunked(4)
                    list(buttons, bot, update.chatId(), "Ваши символы, нажмите на символ чтобы отписаться")
                }
            }

            parentInlButton("Ваши таймфреймы / Удаление") {
                action = { bot, update ->
                    val buttons = BotHelper.getTimeFrames(update.chatId())
                        .map { InlineButton(it, Cmd("rm_tf", mapOf("tf" to it)), "") }.chunked(1)
                    list(buttons, bot, update.chatId(), "Нажмите на таймфрейм чтобы отписаться")
                }
            }

            inlButton("Добавить таймфрейм", Cmd("add_tf_menu"), "Добавьте таймфрейм") {
                rowSize = 1
                TimeFrame.values().forEach { tf ->
                    subInlButton(tf.name, Cmd("add_tf", mapOf("tf" to tf.name)), "tf") {}
                }
            }
        }
    }

    private fun MenuItem.makeTechMenu() {
        menuItem("Технический анализ") {
            rowSize = 2
            menuItem("Демарк секвента") {
                title = "Выберите таймфрейм для демарк секвенты"
                TimeFrame.values().forEach { tf ->
                    parentInlButton(tf.name) {
                        action = { bot, update ->
                            val bts = makeButtons(
                                "dema",
                                update.chatId(),
                                tf,
                                "Выберите тикер для демарк секвенты"
                            )
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, update)
                            } else {
                                list(bts.chunked(4), bot, update.chatId(), "Компании")
                            }
                        }
                    }
                }
            }

            menuItem("Тренд линии") {
                title = "Выберите таймфрейм для тренд линий"
                TimeFrame.values().forEach { tf ->
                    parentInlButton(tf.name) {
                        action = { bot, update ->
                            val bts =
                                makeButtons("tl", update.chatId(), tf, "Выберите тикер для тренд линии")
                            if (bts.isEmpty()) {
                                emtyListMsg(bot, update)
                            } else {
                                list(bts.chunked(4), bot, update.chatId(), "Выберите компанию для тренд линий")
                            }
                        }
                    }
                }
            }
            menuItem("Главное меню") {}
        }
    }

    private fun emtyListMsg(bot: Bot, update: Update) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions["Настройки"]!!(bot, update)
    }

    private fun makeButtons(
        cmd: String,
        chatId: ChatId,
        tf: TimeFrame,
        title: String
    ): List<InlineButton> {
        val bts = BotHelper.getSubscriptions(chatId.getId().toInt()).map { instrId ->
            InlineButton(
                instrId.code,
                Cmd(cmd, mapOf("id" to instrId.id, "tf" to tf.name)),
                title
            )
        }
        return bts
    }

}