package com.firelib.techbot.menu

import com.firelib.techbot.*
import com.firelib.techbot.command.*
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.ParseMode
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import firelib.core.misc.JsonHelper
import java.util.concurrent.Executors
import java.util.concurrent.ThreadPoolExecutor

class MenuRegistry {
    val menuActions = mutableMapOf<String, (Bot, Update) -> Unit>()

    val commandData = mutableMapOf<String, (Cmd, Bot, Update) -> Unit>()

    companion object {
        val mainMenu = "Главное меню"

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
            measureAndLogTime("processing command ${command}") {
                try {
                    commandData.get(command.handlerName)?.invoke(command, bot, update)
                } catch (e: Exception) {
                    bot.sendMessage(update.chatId(), "error happened ${e.message}", parseMode = ParseMode.MARKDOWN)
                    e.printStackTrace()
                }
            }
        }
    }

    fun listUncat(buttons: List<IButton>, bot: Bot, chatId: ChatId, rowSize: Int, title: String = "") {
        buttons.chunked(40).forEach { chunk ->
            list(chunk.chunked(rowSize), bot, chatId, title)
        }
    }

    fun makeMenu() {
        val root = ParentMenuItem("HOME").apply {
            rowSize = 2
            makeTechMenu()
            makeInstrumentMenu()
            makeFundamentalMenu()
            addActionMenu("Помощь", { bot, update ->
                bot.sendMessage(
                    chatId = update.chatId(),
                    text = "[Поддержка](https://t.me/techBotSupport)",
                    parseMode = ParseMode.MARKDOWN
                )
            })
        }
        root.register(this)
    }

    private fun ParentMenuItem.makeFundamentalMenu() {
        addActionMenu("Фундаментальные данные", { bot, update ->
            val bts = makeButtons(
                FundamentalsCommand.name,
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
        })
        addButtonMenu("Главное меню") {}
    }

    private fun ParentMenuItem.makeInstrumentMenu() {
        addButtonMenu("Инструменты/Подписки") {
            rowSize = 1
            addButton("Добавить символ", "Выберите начальную букву тикера:") {
                rowSize = 4
                MdService.instrByStart.keys.forEach { start ->
                    addButtonToButton(start, "Выберите начальную букву тикера:") {
                        rowSize = 2
                        buttons +=  MdService.instrByStart.getOrDefault(start, emptyList()).map { code ->
                            SimpleButton("(${code.code}) ${code.name}", Cmd(SubHandler.name, mapOf("id" to code.id)))
                        }
                    }
                }
            }

            addActionButton("Ваши символы / Удаление", { bot, update ->
                val buttons = BotHelper.getSubscriptions(update.chatId().getId().toInt()).distinct()
                    .map { SimpleButton(it.code, Cmd(UnsubHandler.name, mapOf("id" to it.id))) }.chunked(4)
                list(buttons, bot, update.chatId(), "Ваши символы, нажмите на символ чтобы отписаться")
            })

            addActionButton("Ваши таймфреймы / Удаление", { bot, update ->
                val buttons = BotHelper.getTimeFrames(update.chatId())
                    .map { SimpleButton(it, Cmd(RmTfHandler.name, mapOf("tf" to it))) }.chunked(1)
                list(buttons, bot, update.chatId(), "Нажмите на таймфрейм чтобы отписаться")
            })

            addButton("Добавить таймфрейм", "Выберите таймфрейм") {
                rowSize = 1
                buttons += TimeFrame.values().map { tf ->
                    SimpleButton(tf.name, Cmd(TfHandler.name, mapOf("tf" to tf.name)))
                }
            }
        }
    }

    private fun ParentMenuItem.makeTechMenu() {
        addParentMenu("Технический анализ") {
            rowSize = 2
            addButtonMenu("Демарк секвента") {
                title = "Выберите таймфрейм для демарк секвенты"
                TimeFrame.values().forEach { tf ->
                    addActionButton(tf.name, { bot, update ->
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
                    })
                }
            }

            addButtonMenu("Тренд линии") {
                title = "Выберите таймфрейм для тренд линий"
                TimeFrame.values().forEach { tf ->
                    addActionButton(tf.name, { bot, update ->
                        val bts = makeButtons("tl", update.chatId(), tf, "Выберите тикер для тренд линии")
                        if (bts.isEmpty()) {
                            emtyListMsg(bot, update)
                        } else {
                            list(bts.chunked(4), bot, update.chatId(), "Выберите компанию для тренд линий")
                        }
                    })
                }
            }
            addButtonMenu("Главное меню") {}
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
    ): List<StaticButtonParent> {
        val bts = BotHelper.getSubscriptions(chatId.getId().toInt()).map { instrId ->
            StaticButtonParent(
                instrId.code,
                Cmd(cmd, mapOf("id" to instrId.id, "tf" to tf.name)),
                title
            )
        }
        return bts
    }

}