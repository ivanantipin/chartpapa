package com.github.kotlintelegrambot.dispatcher

import com.firelib.techbot.BotHelper
import com.firelib.techbot.SymbolsDao
import com.firelib.techbot.command.CommandHandler
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.measureAndLogTime
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import firelib.core.misc.JsonHelper
import firelib.finam.FinamDownloader


class MenuReg {
    val map = mutableMapOf<String, (Bot, Long) -> Unit>()

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
            map[ret.name] = ret.action!!
        } else if (ret.children.isNotEmpty()) {
            map[ret.name] = { bot, chatId ->
                val sm = ret.children.map { KeyboardButton(it.name) }.chunked(ret.rowSize)
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
                bot.sendMessage(
                    chatId = chatId,
                    text = ret.name,
                    replyMarkup = keyboardMarkup
                )
            }
        } else {
            map[ret.name] = { bot, chatId ->
                list(ret.buttons.chunked(ret.rowSize), bot, chatId, ret.title)
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

    fun processData(data: String, bot: Bot, update: Update) {
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

    fun list(buttons: List<List<InlineButton>>, bot: Bot, chatId: Long, title: String) {
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

    fun listUncat(buttons: List<InlineButton>, bot: Bot, chatId: Long, rowSize: Int) {
        buttons.groupBy { it.title }.forEach {
            list(it.value.chunked(rowSize), bot, chatId, it.key)
        }
    }


    fun makeMenu() {
        val root = MenuItem("HOME").apply {
            menuItem("Технический анализ") {
                rowSize = 2
                menuItem("Демарк секвента") {
                    title = "Выберите таймфрейм для демарк секвенты"
                    TimeFrame.values().forEach { tf ->
                        inlButton(tf.name, Cmd("dema_tf_${tf.name}"), "выберите тикер") {
                            rowSize = 4
                            SymbolsDao.all.forEach { instr ->

                                val mkt = "${FinamDownloader.FinamMarket.decode(instr.market)}"

                                subInlButton(
                                    instr.code,
                                    Cmd("dema", mapOf("ticker" to instr.code, "tf" to tf.name)),
                                    mkt
                                ) {}
                            }
                        }
                    }
                }

                menuItem("Тренд линии") {
                    title = "Выберите таймфрейм для тренд линий"
                    TimeFrame.values().forEach { tf ->
                        inlButton(tf.name, Cmd("tl_tf_${tf.name}"), "выберите тикер") {
                            rowSize = 4
                            SymbolsDao.all.forEach { instr ->
                                val mkt = "${FinamDownloader.FinamMarket.decode(instr.market)}"
                                subInlButton(
                                    instr.code,
                                    Cmd("tl", mapOf("ticker" to instr.code, "tf" to tf.name)),
                                    mkt
                                ) {}
                            }
                        }
                    }
                }

                menuItem("Горизонтальные уровни") {
                    title = "Выберите таймфрейм для горизонтальных уровней"
                    TimeFrame.values().forEach { tf ->
                        inlButton(tf.name, Cmd("lvl_tf_${tf.name}"), "выберите тикер") {
                            rowSize = 4
                            SymbolsDao.all.forEach { instr ->
                                val mkt = "${FinamDownloader.FinamMarket.decode(instr.market)}"
                                subInlButton(
                                    instr.code,
                                    Cmd("lvl", mapOf("ticker" to instr.code, "tf" to tf.name)),
                                    mkt
                                ) {}
                            }
                        }
                    }
                }

                menuItem("Подписки на сигналы") {
                    title = "Выберите действие"
                    rowSize = 1
                    inlButton("Ваши подписки на сигналы", Cmd("my_sub"), "My subscriptions") {
                        action = { bot, update ->
                            bot.sendMessage(
                                chatId = update.chatId(),
                                text = BotHelper.displaySubscriptions(update.chatId().toInt()),
                                parseMode = ParseMode.MARKDOWN
                            )
                        }
                    }
                    inlButton("Отписаться от сигналов", Cmd("unsub_menu"), "") {
                        action = { bot, update ->
                            val buttons = BotHelper.getSubscriptions(update.chatId().toInt()).distinct()
                                .map { InlineButton(it, Cmd("unsub", mapOf("ticker" to it)), "") }.chunked(4)
                            list(buttons, bot, update.chatId(), "Нажмите на тикер чтобы отписаться")
                        }
                    }
                    inlButton(
                        "Подписаться на сигналы",
                        Cmd("sub_menu"),
                        "выберите тикер чтобы подписаться на сигналы:"
                    ) {
                        rowSize = 4
                        SymbolsDao.all.forEach { instr ->
                            val mkt = "${FinamDownloader.FinamMarket.decode(instr.market)}"
                            subInlButton(instr.code, Cmd("sub", mapOf("ticker" to instr.code)), mkt) {}
                        }
                    }

                    inlButton("Ваши таймфреймы", Cmd("display_tf_menu"), "Ваши таймфреймы:") {
                        action = { bot, update ->
                            bot.sendMessage(
                                update.chatId(),
                                BotHelper.displayTimeFrames(update.chatId().toInt()),
                                parseMode = ParseMode.MARKDOWN
                            )
                        }
                    }

                    inlButton(
                        "Удалить таймфрейм для сигналов",
                        Cmd("rm_tf_menu"),
                        "Ваши таймфреймы, удалите ненужный нажатием"
                    ) {
                        action = { bot, update ->
                            val buttons = BotHelper.getTimeFrames(update.chatId().toInt())
                                .map { InlineButton(it, Cmd("rm_tf", mapOf("tf" to it)), "") }.chunked(1)
                            list(buttons, bot, update.chatId(), "Нажмите на таймфрейм чтобы отписаться")
                        }
                    }

                    inlButton("Добавить таймфрейм", Cmd("add_tf_menu"), "Добавьте таймфрейм") {
                        rowSize = 1
                        TimeFrame.values().forEach { tf ->
                            subInlButton(tf.name, Cmd("add_tf", mapOf("tf" to tf.name)), "") {}
                        }
                    }
                }
                menuItem("Главное меню") {}
            }
            menuItem("Помощь") {
                action = { bot, chatIt ->
                    bot.sendMessage(
                        chatId = chatIt,
                        text = "[Поддержка](https://t.me/techBotSupport)",
                        parseMode = ParseMode.MARKDOWN
                    )
                }
            }
        }
        reg(root)
    }

}


fun Update.chatId(): Long {
    val fromUser = this.message?.from ?: this.callbackQuery?.from!!
    return fromUser.id
}

fun Update.fromUser(): User {
    return this.message?.from ?: this.callbackQuery?.from!!
}

data class Cmd(val name: String, val opts: Map<String, String> = mutableMapOf()) {
    fun tickerAndTf(): Pair<String, TimeFrame> {
        return Pair(opts["ticker"]!!, TimeFrame.valueOf(opts["tf"]!!))
    }
}

class InlineButton(val name: String, val data: Cmd, val title: String, var rowSize: Int = 3) {
    val buttons: MutableList<InlineButton> = mutableListOf()
    var action: ((bot: Bot, update: Update) -> Unit)? = null
    fun subInlButton(chName: String, data: Cmd, ttl: String, aa: InlineButton.() -> Unit): InlineButton {
        val ret = InlineButton(chName, data, ttl)
        aa(ret)
        buttons += ret
        return ret
    }
}

class MenuItem(
    val name: String,
    val children: MutableList<MenuItem> = mutableListOf(),
    val buttons: MutableList<InlineButton> = mutableListOf(),
    var title: String = name,
    var rowSize: Int = 3

) {

    var action: ((bot: Bot, chatId: Long) -> Unit)? = null

    fun menuItem(chName: String, aa: MenuItem.() -> Unit): MenuItem {
        val ret = MenuItem(chName)
        aa(ret)
        children += ret
        return ret
    }

    fun inlButton(chName: String, data: Cmd, title: String = "", aa: InlineButton.() -> Unit): InlineButton {
        val ret = InlineButton(chName, data, title)
        aa(ret)
        buttons += ret
        return ret
    }
}
