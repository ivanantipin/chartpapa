package com.firelib.techbot

import chart.BreachFinder
import chart.BreachType
import chart.HistoricalBreaches
import chart.SequentaSignals
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant


data class BreachEventKey(val ticker: String, val tf: TimeFrame, val eventTimeMs: Long, val type: BreachType)
data class BreachEvent(val key: BreachEventKey, val photoFile: String)

object UsersNotifier {

    fun start(bot: Bot) {
        TimeFrame.values().forEach { tf ->
            Thread({
                timeSequence(Instant.now(), Interval.Min10, 10000).forEach {
                    try {
                        check(bot, tf, 5)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }

                }
            }, tf.name + "_breach_notifier").start()

        }
    }

    fun check(bot: Bot, timeFrame: TimeFrame, breachWindow: Int) {
        try {
            transaction {
                val subscribed = Subscriptions.select { Subscriptions.timeframe eq timeFrame.name }
                    .map { it[Subscriptions.ticker] }.distinct()

                val existingEvents = loadExistingBreaches().map { it.key }.toSet()

                val breaches =
                    SymbolsDao.available().map { it.code }.filter { subscribed.contains(it) }.flatMap { ticker ->
                        BreachFinder.findNewBreaches(
                            ticker,
                            timeFrame,
                            breachWindow,
                            existingEvents
                        ) + BreachFinder.findLevelBreaches(
                            ticker,
                            timeFrame,
                            breachWindow,
                            existingEvents
                        ) + SequentaSignals.checkSignals(ticker, timeFrame, breachWindow, existingEvents)
                    }

                breaches.forEach {
                    notify(it, bot)
                }

                updateDatabase("insert breaches") {
                    BreachEvents.batchInsert(breaches) {
                        this[BreachEvents.ticker] = it.key.ticker
                        this[BreachEvents.timeframe] = it.key.tf.name
                        this[BreachEvents.eventTimeMs] = it.key.eventTimeMs
                        this[BreachEvents.photoFile] = it.photoFile
                        this[BreachEvents.eventType] = it.key.type.name
                    }
                }.get()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    fun loadExistingBreaches(): List<BreachEvent> {
        return BreachEvents.select { BreachEvents.eventTimeMs greater System.currentTimeMillis() - 10 * 24 * 3600_000L }
            .map {
                val key = BreachEventKey(
                    ticker = it[BreachEvents.ticker],
                    TimeFrame.valueOf(it[BreachEvents.timeframe]),
                    it[BreachEvents.eventTimeMs],
                    BreachType.valueOf(it[BreachEvents.eventType])
                )
                BreachEvent(key, photoFile = it[BreachEvents.photoFile])
            }
    }

    fun notify(be: BreachEvent, bot: Bot) {
        Subscriptions.select {
            Subscriptions.ticker eq be.key.ticker and (Subscriptions.timeframe eq be.key.tf.name)
        }.forEach {
            mainLogger.info("notifiying user ${it}")
            val response = bot.sendPhoto(it[Subscriptions.user].toLong(), File(be.photoFile))
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }
}

fun main() {
    initDatabase()
    //UpdateSensitivities.updateSensitivties()

    val bot = makeBot(TABot())
    UsersNotifier.check(bot, TimeFrame.M30, 20)
}