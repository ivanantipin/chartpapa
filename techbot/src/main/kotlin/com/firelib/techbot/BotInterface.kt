package com.firelib.techbot

import com.firelib.techbot.domain.UserId

interface BotInterface {
    suspend fun sendPhoto(img : ByteArray, users: List<UserId>)
}

