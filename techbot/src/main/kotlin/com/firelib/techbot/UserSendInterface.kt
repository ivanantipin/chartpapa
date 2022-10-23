package com.firelib.techbot

import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.domain.UserId

interface BotInterface {
    fun sendBreachEvent(be: BreachEventKey, img : ByteArray, users: List<UserId>)
}

