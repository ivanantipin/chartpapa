package com.firelib.techbot

import com.firelib.techbot.domain.UserId

interface BotInterface {
    fun sendBreachEvent(img : ByteArray, users: List<UserId>)
}

