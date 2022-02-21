package com.firelib.techbot

import chart.BreachType
import chart.SignalType
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.macd.RsiBolingerSignals
import com.firelib.techbot.sequenta.SequentaSignals
import com.firelib.techbot.tdline.TdLineSignals
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.ChatId
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.time.Instant

@JvmInline
value class UserId(val id: Long)

data class NotifyGroup(
    val ticker: InstrId,
    val signalType: SignalType,
    val timeFrame: TimeFrame,
    val settings: Map<String, String>
)

object UsersNotifier {

    fun getUserTickers(): Map<UserId, List<InstrId>> {
        return BotHelper.getSubscriptions()
    }

    fun getUserSettings(): Map<UserId, List<Map<String, String>>> {
        return BotHelper.getAllSettings()
    }

    fun getUserTimeframes(): Map<UserId, List<TimeFrame>> {
        return BotHelper.getTimeFrames()
    }

    fun getUserSignalTypes(): Map<UserId, List<SignalType>> {
        return BotHelper.getSignalTypes()
    }

    fun getNotifyGroups(): Map<NotifyGroup, List<UserId>> {
        val signalTypes = getUserSignalTypes()
        val userSettings = getUserSettings()
        val userTickers = getUserTickers()
        val pairs = getUserTimeframes().flatMap { (userId, timeFrames) ->
            timeFrames.flatMap { timeFrame ->
                userTickers.getOrDefault(userId, emptyList()).flatMap { ticker ->
                    signalTypes.getOrDefault(userId, emptyList()).map { signalType ->
                        val settings = userSettings.getOrDefault(userId, emptyList())
                        val macdSettings = settings.find { it["command"] == signalType.settingsName } ?: emptyMap()
                        NotifyGroup(ticker, signalType, timeFrame, macdSettings) to userId
                    }
                }
            }
        }
        return pairs.groupBy { it.first }.mapValues { it.value.map { e -> e.second }.distinct() }
    }

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    fun start(bot: Bot) {
        Thread({
            timeSequence(Instant.now(), Interval.Min10, 10000).forEach {
                try {
                    measureAndLogTime("signal checking", {
                        check(bot, 2)
                    })
                } catch (e: Exception) {
                    log.error("error in user notifier", e)
                }

            }
        }, "breach_notifier").start()
    }

    fun check(bot: Bot, breachWindow: Int) {
        transaction {
            val existingEvents = loadExistingBreaches().map { it.key }.toSet()
            getNotifyGroups().forEach { (group, users) ->
                try {
                    measureAndLogTime("group ${group} processing took", {
                        processGroup(group, breachWindow, existingEvents, bot, users)
                    })
                } catch (e: Exception) {
                    log.error("error notifying group ${group}", e)
                }
            }
        }
    }


    private fun processGroup(
        group: NotifyGroup,
        breachWindow: Int,
        existingEvents: Set<BreachEventKey>,
        bot: Bot,
        users: List<UserId>
    ) {
        val instrId = group.ticker
        val timeFrame = group.timeFrame

        val breaches = SignalType.values().flatMap {
            it.signalGenerator.checkSignals(
                instrId,
                timeFrame,
                breachWindow,
                existingEvents,
                group.settings)
        }

        breaches.forEach {
            notify(it, bot, users)
        }

        if (breaches.isNotEmpty()) {
            updateDatabase("insert breaches ${breaches.size}") {
                BreachEvents.batchInsert(breaches) {
                    this[BreachEvents.instrId] = it.key.instrId
                    this[BreachEvents.timeframe] = it.key.tf.name
                    this[BreachEvents.eventTimeMs] = it.key.eventTimeMs
                    this[BreachEvents.photoFile] = it.photoFile
                    this[BreachEvents.eventType] = it.key.type.name
                }
            }.get()
        }
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

    fun notify(be: BreachEvent, bot: Bot, users: List<UserId>) {
        users.forEach { userId ->
            mainLogger.info("notifiying user ${userId}")
            val response = bot.sendPhoto(ChatId.fromId(userId.id), File(be.photoFile))
            if (response.second != null) {
                response.second!!.printStackTrace()
            }
        }
    }
}

