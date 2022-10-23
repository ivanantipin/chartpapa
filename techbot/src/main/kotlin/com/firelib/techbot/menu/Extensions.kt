package com.firelib.techbot.menu

import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import java.util.concurrent.atomic.AtomicLong

fun Update.chatId(): ChatId {
    return ChatId.fromId(fromUser().id)
}

fun Update.fromUser(): User {
    return this.message?.from ?: this.callbackQuery?.from!!
}

val cnt = AtomicLong();

fun getCmdName(): String {
    return "cmd${cnt.incrementAndGet()}";
}


