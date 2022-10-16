package com.firelib.techbot

import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.domain.UserId

interface BotInterface {
    fun sendBreachEvent(be: BreachEvent, users: List<UserId>)
}

