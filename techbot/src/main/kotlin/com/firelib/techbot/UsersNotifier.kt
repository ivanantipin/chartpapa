package com.firelib.techbot

import chart.BreachFinder
import chart.BreachType
import chart.SequentaSignals
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant


data class BreachEventKey(val ticker: String, val tf: TimeFrame, val eventTimeMs: Long, val type: BreachType)
data class BreachEvent(val key: BreachEventKey, val photoFile: String)

object UsersNotifier {

    val log = LoggerFactory.getLogger(UsersNotifier::class.java)

    fun start(bot: Bot) {
        TimeFrame.values().forEach { tf ->
            Thread({
                timeSequence(Instant.now(), tf.interval, 10000).forEach {
                    try {
                        check(bot, tf, 2)
                    } catch (e: Exception) {
                        log.error("error in user notifier", e)
                    }

                }
            }, tf.name + "_breach_notifier").start()
        }
    }

    fun check(bot: Bot, timeFrame: TimeFrame, breachWindow: Int) {
        try {
            transaction {

                val subscribed = Subscriptions.selectAll().map { it[Subscriptions.ticker] }.distinct()

                val existingEvents = loadExistingBreaches().map { it.key }.toSet()

                val breaches =
                    MdService.liveSymbols.map { it.code }.filter { subscribed.contains(it) }.flatMap { ticker ->
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
            log.error("errer in user notifier checking timeframe ${timeFrame}", e)
        }
        println("transaction finished")
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
        Subscriptions
            .join(TimeFrames, joinType = JoinType.INNER, Subscriptions.user, TimeFrames.user,
                { Subscriptions.ticker eq be.key.ticker and (TimeFrames.tf eq be.key.tf.name) })
            .selectAll().forEach {
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

    TimeFrame.values().forEach { tf ->
        UsersNotifier.check(bot, tf, 5)
    }

}