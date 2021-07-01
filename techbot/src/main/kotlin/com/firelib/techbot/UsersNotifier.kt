package com.firelib.techbot

import chart.BreachFinder
import chart.BreachType
import chart.SequentaSignals
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant


data class BreachEventKey(val instrId: String, val tf: TimeFrame, val eventTimeMs: Long, val type: BreachType)
data class BreachEvent(val key: BreachEventKey, val photoFile: String)

object UsersNotifier {

    val log = LoggerFactory.getLogger(UsersNotifier::class.java)

    fun start(bot: Bot) {
        TimeFrame.values().forEach { tf ->
            Thread({
                timeSequence(Instant.now(), tf.interval, 10000).forEach {
                    try {
                        log.info("checking ${tf}")
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

                val pares = Subscriptions.selectAll().map {
                    it[Subscriptions.ticker] to it[Subscriptions.market]
                }.distinct()

                val existingEvents = loadExistingBreaches().map { it.key }.toSet()

                val breaches =
                    MdService.liveSymbols.filter { pares.contains(it.code to it.market) }.flatMap { instrId ->
                        BreachFinder.findNewBreaches(
                            instrId,
                            timeFrame,
                            breachWindow,
                            existingEvents
                        ) + BreachFinder.findLevelBreaches(
                            instrId,
                            timeFrame,
                            breachWindow,
                            existingEvents
                        ) + SequentaSignals.checkSignals(instrId, timeFrame, breachWindow, existingEvents)
                    }

                if(breaches.isNotEmpty()){
                    log.info("found ${breaches.size}")
                }

                breaches.forEach {
                    notify(it, bot)
                }

                updateDatabase("insert breaches") {
                    BreachEvents.batchInsert(breaches) {
                        this[BreachEvents.instrId] = it.key.instrId
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
                    instrId = it[BreachEvents.instrId],
                    TimeFrame.valueOf(it[BreachEvents.timeframe]),
                    it[BreachEvents.eventTimeMs],
                    BreachType.valueOf(it[BreachEvents.eventType])
                )
                BreachEvent(key, photoFile = it[BreachEvents.photoFile])
            }
    }

    fun notify(be: BreachEvent, bot: Bot) {
        val instrId = MdService.byId(be.key.instrId)
        Subscriptions
            .join(TimeFrames, joinType = JoinType.INNER, Subscriptions.user, TimeFrames.user,
                { Subscriptions.ticker eq instrId.code and (Subscriptions.market eq instrId.market) and (TimeFrames.tf eq be.key.tf.name) })
            .selectAll().forEach {
                mainLogger.info("notifiying user ${it}")
                val response = bot.sendPhoto(ChatId.fromId(it[Subscriptions.user].toLong()), File(be.photoFile))
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
        UsersNotifier.check(bot, tf, 20)
    }

}