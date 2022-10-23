package com.firelib.techbot

import com.firelib.techbot.menu.langCode
import com.firelib.techbot.persistence.TimeFrames
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.io.FileOutputStream

fun ChatId.getId(): Long {
    return (this as ChatId.Id).id
}

object BotHelper {

    fun displayTimeFrames(uid: User): String {
        return transaction {
            val header = MsgLocalizer.YourTimeframes
            val resp = TimeFrames.select {
                TimeFrames.user eq uid.id
            }.map { it[TimeFrames.tf] }.sorted().joinToString(separator = "\n")
            header.toLocal(uid.langCode()) + resp
        }
    }

    fun saveFile(bytes: ByteArray, fileName: String) {
        File(fileName).parentFile.mkdirs()
        FileOutputStream(fileName).use {
            it.write(bytes)
        }
    }
}

