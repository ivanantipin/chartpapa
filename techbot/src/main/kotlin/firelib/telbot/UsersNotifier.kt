package com.github.kotlintelegrambot.echo.com.firelib.telbot

import com.firelib.sub.BreachEvents
import com.firelib.sub.Subscriptions
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.echo.chart.ImageService
import firelib.telbot.SymbolsDao
import firelib.telbot.TimeFrame
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File


data class BreachEventKey(val ticker : String, val tf : TimeFrame, val eventTimeMs : Long)
data class BreachEvent(val key : BreachEventKey, val photoFile : String)

object UsersNotifier {

    fun check(bot : Bot){
        try {
            transaction {
                val subscribed = Subscriptions.selectAll().withDistinct(true).map { it[Subscriptions.ticker] }
                val breaches = SymbolsDao.available().map { it.code }.filter { subscribed.contains(it) }.flatMap{ ticker->
                    //fixme timeframes
                    ImageService.findBreaches(ticker, TimeFrame.H)
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
                    it[BreachEvents.eventTimeMs]
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