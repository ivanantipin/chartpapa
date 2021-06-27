package com.github.kotlintelegrambot.dispatcher

import com.github.kotlintelegrambot.entities.*
import java.util.concurrent.atomic.AtomicLong


fun Update.chatId(): Long {
    val fromUser = this.message?.from ?: this.callbackQuery?.from!!
    return fromUser.id
}

fun Update.fromUser(): User {
    return this.message?.from ?: this.callbackQuery?.from!!
}

val cnt = AtomicLong();

fun getCmdName(): String {
    return "cmd${cnt.incrementAndGet()}";
}


