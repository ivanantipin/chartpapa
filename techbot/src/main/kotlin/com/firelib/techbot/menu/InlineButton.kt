package com.firelib.techbot.menu

import com.firelib.techbot.command.Cmd
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

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