package com.firelib.techbot

import com.firelib.techbot.chart.domain.HOptions
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.persistence.Settings
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.User
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import firelib.core.misc.JsonHelper
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

interface SignalGenerator {

    fun signalType(): SignalType

    fun checkSignals(
        instr: InstrId,
        tf: TimeFrame,
        threshold : Instant,
        settings: Map<String, String>,
        ohlcs : List<Ohlc>
    ): List<Pair<Instant, HOptions>>

    fun drawPicture(instr: InstrId, tf: TimeFrame, settings: Map<String, String>, ohlcs : List<Ohlc>): HOptions

    fun fetchSettings(userId: Long): Map<String, String> {
        val value = transaction {
            Settings.select { (Settings.user eq userId) and (Settings.name eq signalType().name) }
                .map { it[Settings.value] }.firstOrNull()
        }
        return if (value == null) emptyMap() else JsonHelper.fromJson(value)
    }

    fun validate(split: List<String>): Boolean {
        return true
    }

    fun parsePayload(split: List<String>): Map<String, String> {
        return emptyMap()
    }

    fun displayHelp(bot: Bot, user: User) {

    }

}