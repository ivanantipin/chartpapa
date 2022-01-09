package com.firelib.techbot.menu

import com.github.kotlintelegrambot.entities.*
import java.util.concurrent.atomic.AtomicLong


fun Update.chatId(): ChatId {
    val fromUser = this.message?.from ?: this.callbackQuery?.from!!
    return ChatId.fromId(fromUser.id)
}

fun Update.fromUser(): User {
    return this.message?.from ?: this.callbackQuery?.from!!
}

val cnt = AtomicLong();

fun getCmdName(): String {
    return "cmd${cnt.incrementAndGet()}";
}


