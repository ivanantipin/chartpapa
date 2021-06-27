package com.firelib.techbot

import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.dispatcher.getCmdName
import com.github.kotlintelegrambot.entities.Update

class MenuItem(
    val name: String,
    val children: MutableList<MenuItem> = mutableListOf(),
    val buttons: MutableList<InlineButton> = mutableListOf(),
    var title: String = name,
    var rowSize: Int = 3

) {

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

}