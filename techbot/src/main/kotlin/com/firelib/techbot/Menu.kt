package com.github.kotlintelegrambot.dispatcher

import com.firelib.techbot.BotHelper
import com.firelib.techbot.MdService
import com.firelib.techbot.command.CommandHandler
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.measureAndLogTime
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.*
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import firelib.core.misc.JsonHelper
import java.util.concurrent.atomic.AtomicLong


class MenuReg {
    val menuActions = mutableMapOf<String, (Bot, Long) -> Unit>()

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

            menuActions[ret.name] = { bot, chatId ->
                val sm = ret.children.map { KeyboardButton(it.name) }.chunked(ret.rowSize)
                val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
                bot.sendMessage(
                    chatId = chatId,
                    text = ret.name,
                    replyMarkup = keyboardMarkup
                )
            }
        } else {
            menuActions[ret.name] = { bot, chatId ->
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
            menuItem("Технический анализ") {
                rowSize = 2
                menuItem("Демарк секвента") {
                    title = "Выберите таймфрейм для демарк секвенты"
                    TimeFrame.values().forEach { tf ->
                        parentInlButton(tf.name) {
                            action = { bot, update ->
                                val bts = makeButtons(
                                    "dema",
                                    update.chatId().toInt(),
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
                                    makeButtons("tl", update.chatId().toInt(), tf, "Выберите тикер для тренд линии")
                                if (bts.isEmpty()) {
                                    emtyListMsg(bot, update)
                                } else {
                                    list(bts.chunked(4), bot, update.chatId(), "Компании")
                                }
                            }
                        }
                    }
                }

                menuItem("Горизонтальные уровни") {
                    title = "Выберите таймфрейм для горизонтальных уровней"
                    TimeFrame.values().forEach { tf ->
                        parentInlButton(tf.name) {
                            action = { bot, update ->
                                val bts = makeButtons(
                                    "lvl",
                                    update.chatId().toInt(),
                                    tf,
                                    "Выберите тикер для горизонтальных линий"
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
                menuItem("Главное меню") {}
            }

            menuItem("Настройки") {
                rowSize = 2
                parentInlButton("Ваши символы / Редактировать") {
                    action = { bot, update ->
                        val buttons = BotHelper.getSubscriptions(update.chatId().toInt()).distinct()
                            .map { InlineButton(it, Cmd("unsub", mapOf("ticker" to it)), "") }.chunked(4)
                        list(buttons, bot, update.chatId(), "Ваши символы, нажмите на символ чтобы отписаться")
                    }
                }

                parentInlButton("Добавить символ") {
                    rowSize = 4

                    MdService.instrByStart.keys.forEach { start ->
                        subInlButton(start, "Выберите начальную букву тикера:") {
                            rowSize = 2
                            MdService.instrByStart.getOrDefault(start, emptyList()).forEach { code ->
                                subInlButton(
                                    "(${code.code}) ${code.name}",
                                    Cmd("sub", mapOf("ticker" to code.code, "market" to code.market)),
                                    "Выберите компанию для добавления:"
                                ) {}
                            }
                        }
                    }
                }

                parentInlButton("Ваши таймфреймы / Редактировать") {
                    action = { bot, update ->
                        val buttons = BotHelper.getTimeFrames(update.chatId().toInt())
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

    private fun emtyListMsg(bot: Bot, update: Update) {
        bot.sendMessage(
            chatId = update.chatId(),
            text = "Ваш список символов пуст, используйте *Добавить символ* меню",
            parseMode = ParseMode.MARKDOWN
        )

        menuActions["Настройки"]!!(bot, update.chatId())
    }

    private fun makeButtons(
        cmd: String,
        chatId: Int,
        tf: TimeFrame,
        title: String
    ): List<InlineButton> {
        val bts = BotHelper.getSubscriptions(chatId).map { code ->
            InlineButton(
                code,
                Cmd(cmd, mapOf("ticker" to code, "tf" to tf.name)),
                title
            )
        }
        return bts
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

    fun subInlButton(chName: String, ttl: String, aa: InlineButton.() -> Unit): InlineButton {
        val ret = InlineButton(chName, Cmd(getCmdName()), ttl)
        aa(ret)
        buttons += ret
        return ret
    }


}

val cnt = AtomicLong();

fun getCmdName(): String {
    return "cmd${cnt.incrementAndGet()}";
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
        require(buttons.isEmpty(), { "to add menu item, buttons items must be empty" })
        val ret = MenuItem(chName)
        aa(ret)
        children += ret
        return ret
    }

    fun inlButton(chName: String, data: Cmd, title: String = "", aa: InlineButton.() -> Unit): InlineButton {

        require(children.isEmpty(), { "to add inline button, menu items must be empty" })

        val ret = InlineButton(chName, data, title)
        aa(ret)
        buttons += ret
        return ret
    }


    fun parentInlButton(chName: String, aa: InlineButton.() -> Unit): InlineButton {

        require(children.isEmpty(), { "to add inline button, menu items must be empty" })

        val ret = InlineButton(chName, Cmd(getCmdName()), title)
        aa(ret)
        buttons += ret
        return ret
    }

}
