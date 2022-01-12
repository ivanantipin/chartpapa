package com.firelib.techbot.menu

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

class MenuItem(
    val name: String,
    val children: MutableList<IMenu> = mutableListOf(),
    var rowSize: Int = 3
) : IMenu {

    fun addButtonMenu(chName: String, aa: MenuItemButtons.() -> Unit): MenuItemButtons {
        children += MenuItemButtons(chName).apply(aa)
        return children.last() as MenuItemButtons
    }

    fun addParentMenu(chName: String, aa: MenuItem.() -> Unit) {
        children += MenuItem(chName).apply(aa)
    }

    fun addActionMenu(chName: String, action: ((bot: Bot, update: Update) -> Unit)) {
        children += ActionMenuItem(chName, action)
    }

    override fun name(): String {
        return name
    }

    override fun act(bot: Bot, update: Update) {
        val sm = children.map { KeyboardButton(it.name()) }.chunked(rowSize)
        val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
        bot.sendMessage(
            chatId = update.chatId(),
            text = name,
            replyMarkup = keyboardMarkup
        )
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = this::act
        children.forEach{it.register(registry)}
    }

}

