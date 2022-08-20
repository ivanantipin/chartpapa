package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.Msg
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton

class ParentMenuItem(
    val name: Msg,
    val children: MutableList<IMenu> = mutableListOf(),
    var rowSize: Int = 3
) : IMenu {

    fun addButtonMenu(chName: Msg, aa: MenuItemButtons.() -> Unit): MenuItemButtons {
        children += MenuItemButtons(chName, title= {"Naa"}).apply(aa)
        return children.last() as MenuItemButtons
    }

    fun addParentMenu(chName: Msg, aa: ParentMenuItem.() -> Unit) {
        children += ParentMenuItem(chName).apply(aa)
    }

    fun addActionMenu(chName: Msg, action: ((bot: Bot, update: Update) -> Unit)) {
        children += ActionMenuItem(chName, action)
    }

    override fun name(): Msg {
        return name
    }

    private fun listSubMenus(bot: Bot, update: Update) {
        val sm = children.map { KeyboardButton( Msg.getMsg(update.langCode(),  it.name()) ) }.chunked(rowSize)
        val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
        bot.sendMessage(
            chatId = update.chatId(),
            text = Msg.getMsg(update.langCode(), name) ,
            replyMarkup = keyboardMarkup
        )
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = this::listSubMenus
        children.forEach { it.register(registry) }
    }

}

fun Update.langCode() : Langs{
    return Langs.valueOf(this.fromUser().languageCode!!.uppercase())
}