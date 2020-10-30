package com.firelib.techbot

import chart.BreachFinder
import chart.BreachType
import com.firelib.techbot.domain.TimeFrame
import com.github.kotlintelegrambot.Bot
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.time.Instant


data class BreachEventKey(val ticker : String, val tf : TimeFrame, val eventTimeMs : Long, val type : BreachType)
data class BreachEvent(val key : BreachEventKey, val photoFile : String)

object UsersNotifier {

    fun start(bot : Bot){
        TimeFrame.values().forEach {tf->
            Thread({
                timeSequence(Instant.now(), Interval.Min10, 10000).forEach {
                    try {
                        check(bot, tf, 5)
                    }catch (e : Exception){
                        e.printStackTrace()
                    }

                }
            }, tf.name + "_breach_notifier").start()

        }
    }

    fun check(bot : Bot, timeFrame: TimeFrame, breachWindow : Int){
        try {
            transaction {
                val subscribed = Subscriptions.select { Subscriptions.timeframe eq timeFrame.name }
                    .map { it[Subscriptions.ticker] }.distinct()

                println("subsc " + subscribed)

                val breaches = SymbolsDao.available().map { it.code}.filter {subscribed.contains(it) }.flatMap{ ticker->
                    //fixme timeframes
                    BreachFinder.findBreaches(ticker, timeFrame, breachWindow)
                }

                println("breaches found ${breaches.joinToString (separator = "\n")}")

                val existingEvents = loadExistingBreaches()

                println("existing breaches ${existingEvents}")

                val keys = existingEvents.map { it.key }.toSet()

                val newBreaches = breaches.filter { !keys.contains(it.key) }

                println("new breaches ${newBreaches}")

                newBreaches.forEach {
                    process(it, bot)
                }

                BreachEvents.batchInsert(newBreaches){
                    this[BreachEvents.ticker] = it.key.ticker
                    this[BreachEvents.timeframe] = it.key.tf.name
                    this[BreachEvents.eventTimeMs] = it.key.eventTimeMs
                    this[BreachEvents.photoFile] = it.photoFile
                    this[BreachEvents.eventType] = BreachType.TREND_LINE.name
                }
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }

    fun loadExistingBreaches(): List<BreachEvent> {
        val existingEvents =
            BreachEvents.select { BreachEvents.eventTimeMs greater System.currentTimeMillis() - 200 * 24 * 3600_000L }.map {
                val key = BreachEventKey(
                    ticker = it[BreachEvents.ticker],
                    TimeFrame.valueOf(it[BreachEvents.timeframe]),
                    it[BreachEvents.eventTimeMs],
                    BreachType.valueOf(it[BreachEvents.eventType])
                )
                BreachEvent(key, photoFile = it[BreachEvents.photoFile])
            }
        return existingEvents
    }

    fun process(be: BreachEvent, bot : Bot){
        Subscriptions.select{
            Subscriptions.ticker eq be.key.ticker and (Subscriptions.timeframe eq be.key.tf.name)
        }.forEach {
            println("notifiying user ${it}")
            val response = bot.sendPhoto(it[Subscriptions.user].toLong(), File(be.photoFile))
            if(response.second != null){
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