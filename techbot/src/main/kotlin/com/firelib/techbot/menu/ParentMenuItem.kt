package com.firelib.techbot.menu

import com.firelib.techbot.Langs
import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.persistence.Users
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.KeyboardReplyMarkup
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import com.github.kotlintelegrambot.entities.keyboard.KeyboardButton
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

class ParentMenuItem(
    val name: MsgLocalizer,
    val children: MutableList<BotMenu> = mutableListOf(),
    var rowSize: Int = 3
) : BotMenu {

    fun addButtonMenu(chName: MsgLocalizer, aa: MenuItemButtons.() -> Unit): MenuItemButtons {
        children += MenuItemButtons(chName, title = { "Naa" }).apply(aa)
        return children.last() as MenuItemButtons
    }

    fun addParentMenu(chName: MsgLocalizer, aa: ParentMenuItem.() -> Unit) {
        children += ParentMenuItem(chName).apply(aa)
    }

    fun addActionMenu(chName: MsgLocalizer, action: suspend ((bot: Bot, update: User) -> Unit)) {
        children += ActionMenuItem(chName, action)
    }

    override fun name(): MsgLocalizer {
        return name
    }

    private suspend fun listSubMenus(bot: Bot, update: User) {
        val sm = children.map { KeyboardButton(MsgLocalizer.getMsg(update.langCode(), it.name())) }.chunked(rowSize)
        val keyboardMarkup = KeyboardReplyMarkup(keyboard = sm, resizeKeyboard = true, oneTimeKeyboard = false)
        bot.sendMessage(
            chatId = update.chatId(),
            text = MsgLocalizer.getMsg(update.langCode(), name),
            replyMarkup = keyboardMarkup
        )
    }

    override fun register(registry: MenuRegistry) {
        registry.menuActions[name] = this::listSubMenus
        children.forEach { it.register(registry) }
    }

}

fun User.userId() : UserId{
    return UserId(this.id)
}

fun User.chatId() : ChatId{
    return ChatId.fromId(this.id)
}

fun Update.langCode() : Langs{
    return this.fromUser().langCode()
}

fun User.langCode(): Langs {
    val user = this
    return transaction {
        val langs = Users.select { Users.userId eq user.id }.map {
            it[Users.lang]
        }
        langs.firstOrNull().let {
            if (it == null) {
                Langs.RU
            } else {
                Langs.valueOf(it)
            }
        }
    }
}