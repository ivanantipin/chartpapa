package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

interface IMenu{
    fun act(bot: Bot, update: Update )
}

class MenuItem(
    val name: String,
    val children: MutableList<MenuItem> = mutableListOf(),
    val buttons: MutableList<InlineButton> = mutableListOf(),
    var title: String = name,
    var rowSize: Int = 3

) : IMenu {

    var action: ((bot: Bot, update: Update) -> Unit)? = null

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

    override fun act(bot: Bot, update: Update) {
        if (action != null) {
            action!!(bot, update)
        } else if (children.isNotEmpty()) {
            val sm = children.map { KeyboardButton(it.name) }.chunked(rowSize)
            val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
            bot.sendMessage(
                chatId = update.chatId(),
                text = name,
                replyMarkup = keyboardMarkup
            )

        } else {
            MenuRegistry.list(buttons.chunked(rowSize), bot, update.chatId(), title)
        }

    }

}