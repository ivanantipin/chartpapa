package com.github.kotlintelegrambot.dispatcher

import com.firelib.techbot.debug_token
import com.github.kotlintelegrambot.bot
import com.github.kotlintelegrambot.dispatch
import com.github.kotlintelegrambot.entities.InlineKeyboardMarkup
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN
import com.github.kotlintelegrambot.entities.ParseMode.MARKDOWN_V2
import com.github.kotlintelegrambot.entities.ReplyKeyboardRemove
import com.github.kotlintelegrambot.entities.TelegramFile.ByUrl
import com.github.kotlintelegrambot.entities.dice.DiceEmoji
import com.github.kotlintelegrambot.entities.inlinequeryresults.InlineQueryResult
import com.github.kotlintelegrambot.entities.inlinequeryresults.InputMessageContent
import com.github.kotlintelegrambot.entities.inputmedia.InputMediaPhoto
import com.github.kotlintelegrambot.entities.inputmedia.MediaGroup
import com.github.kotlintelegrambot.entities.keyboard.InlineKeyboardButton
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import com.github.kotlintelegrambot.extensions.filters.Filter
import com.github.kotlintelegrambot.logging.LogLevel
import com.github.kotlintelegrambot.network.fold

fun main(args: Array<String>) {

    val bot = bot {

        token = debug_token
        timeout = 30
        logLevel = LogLevel.Network.Body

        dispatch {
            message(Filter.Sticker) {
                bot.sendMessage(message.chat.id, text = "You have received an awesome sticker \\o/")
            }

            message(Filter.Reply or Filter.Forward) {
                bot.sendMessage(message.chat.id, text = "someone is replying or forwarding messages ...")
            }

            command("start") {

                val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Bot started")

                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }

            command("hello") {

                val result = bot.sendMessage(chatId = update.message!!.chat.id, text = "Hello, world!")

                result.fold({
                    // do something here with the response
                }, {
                    // do something with the error
                })
            }

            command("commandWithArgs") {
                val joinedArgs = args.joinToString()
                val response = if (joinedArgs.isNotBlank()) joinedArgs else "There is no text apart from command!"
                bot.sendMessage(chatId = message.chat.id, text = response)
            }

            command("markdown") {
                val markdownText = "_Cool message_: *Markdown* is `beatiful` :P"
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = markdownText,
                    parseMode = MARKDOWN
                )
            }

            command("markdownV2") {
                val markdownV2Text = """
                    *bold \*text*
                    _italic \*text_
                    __underline__
                    ~strikethrough~
                    *bold _italic bold ~italic bold strikethrough~ __underline italic bold___ bold*
                    [inline URL](http://www.example.com/)
                    [inline mention of a user](tg://user?id=123456789)
                    `inline fixed-width code`
                    ```kotlin
                    fun main() {
                        println("Hello Kotlin!")
                    }
                    ```
                """.trimIndent()
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = markdownV2Text,
                    parseMode = MARKDOWN_V2
                )
            }

            command("inlineButtons") {
                val inlineKeyboardMarkup = InlineKeyboardMarkup.create(
                    listOf(InlineKeyboardButton.CallbackData(text = "Test Inline Button", callbackData = "testButton")),
                    listOf(InlineKeyboardButton.CallbackData(text = "Show alert", callbackData = "showAlert"))
                )
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello, inline buttons!",
                    replyMarkup = inlineKeyboardMarkup
                )
            }

            command("userButtons") {
                val keyboardMarkup =
                    KeyboardReplyMarkup(keyboard = generateUsersButton(), resizeKeyboard = true, oneTimeKeyboard = true)
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello, users buttons!",
                    replyMarkup = keyboardMarkup
                )
            }

            command("mediaGroup") {
                bot.sendMediaGroup(
                    chatId = message.chat.id,
                    mediaGroup = MediaGroup.from(
                        InputMediaPhoto(
                            media = ByUrl("https://www.sngular.com/wp-content/uploads/2019/11/Kotlin-Blog-1400x411.png"),
                            caption = "I come from an url :P"
                        )
                    ),
                    replyToMessageId = message.messageId
                )
            }

            callbackQuery("testButton") {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(chatId, callbackQuery.data)
            }

            callbackQuery(
                callbackData = "showAlert",
                callbackAnswerText = "HelloText",
                callbackAnswerShowAlert = true
            ) {
                val chatId = callbackQuery.message?.chat?.id ?: return@callbackQuery
                bot.sendMessage(chatId, callbackQuery.data)
            }

            text("ping") {
                bot.sendMessage(chatId = message.chat.id, text = "Pong")
            }

            location {
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Your location is (${location.latitude}, ${location.longitude})",
                    replyMarkup = ReplyKeyboardRemove()
                )
            }

            contact {
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Hello, ${contact.firstName} ${contact.lastName}",
                    replyMarkup = ReplyKeyboardRemove()
                )
            }

            channel {
                // Handle channel update
            }

            inlineQuery {
                val queryText = inlineQuery.query

                if (queryText.isBlank() or queryText.isEmpty()) return@inlineQuery

                val inlineResults = (0 until 5).map {
                    InlineQueryResult.Article(
                        id = it.toString(),
                        title = "$it. $queryText",
                        inputMessageContent = InputMessageContent.Text("$it. $queryText"),
                        description = "Add $it. before you word"
                    )
                }
                bot.answerInlineQuery(inlineQuery.id, inlineResults)
            }

            photos {
                bot.sendMessage(
                    chatId = message.chat.id,
                    text = "Wowww, awesome photos!!! :P"
                )
            }

            command("diceAsDartboard") {
                bot.sendDice(message.chat.id, DiceEmoji.Dartboard)
            }

            dice {
                bot.sendMessage(
                    message.chat.id,
                    "A dice ${dice.emoji.emojiValue} with value ${dice.value} has been received!"
                )
            }

            telegramError {
                println(error.getErrorMessage())
            }
        }
    }

    bot.startPolling()
}

fun generateUsersButton(): List<List<KeyboardButton>> {
    return listOf(
        listOf(KeyboardButton("TA")),
        listOf(KeyboardButton("FA"))
    )
}