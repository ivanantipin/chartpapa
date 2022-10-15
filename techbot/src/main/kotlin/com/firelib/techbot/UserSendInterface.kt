package com.firelib.techbot

import com.firelib.techbot.breachevent.BreachEvent

interface BotInterface {
    fun sendBreachEvent(be: BreachEvent, users: List<UserId>)
}

