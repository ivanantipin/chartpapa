package com.firelib.techbot

import chart.BreachType
import chart.SignalType
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.domain.TimeFrame
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
import java.util.concurrent.Executors

@JvmInline
value class UserId(val id: Long)

data class NotifyGroup(
    val ticker: InstrId,
    val signalType: SignalType,
    val timeFrame: TimeFrame,
    val settings: Map<String, String>
)

object UsersNotifier {

    fun getNotifyGroups(): Map<NotifyGroup, List<UserId>> {
        val signalTypes = BotHelper.getSignalTypes()
        val userSettings = BotHelper.getAllSettings()
        val userTickers = BotHelper.getSubscriptions()
        val pairs = BotHelper.getTimeFrames().flatMap { (userId, timeFrames) ->
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
            timeSequence(Instant.now(), Interval.Min30, 10000).forEach {
                val enabled = ConfigParameters.NOTIFICATIONS_ENABLED.get().let {
                    it == "true"
                }
                if(!enabled){
                    log.info("Notifications are disabled")
                }else{
                    try {
                        measureAndLogTime("signal checking", {
                            check(bot, 2)
                        })
                    } catch (e: Exception) {
                        log.error("error in user notifier", e)
                    }
                }
            }
        }, "breach_notifier").start()
    }

    val notifyExecutor = Executors.newFixedThreadPool(40)

    fun check(bot: Bot, breachWindow: Int) {
        val existingEvents = loadExistingBreaches().map { it.key }.toSet()
        val notifyGroups = getNotifyGroups()
        log.info("notify group count is ${notifyGroups.size}")
        notifyGroups.map { (group, users) ->
            notifyExecutor.submit{
                try {
                    measureAndLogTime("group ${group} processing took", {
                        processGroup(group, breachWindow, existingEvents, bot, users)
                    })
                } catch (e: Exception) {
                    log.error("error notifying group ${group}", e)
                }
            }
        }.forEach { it.get() }
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

        val breaches = group.signalType.signalGenerator.checkSignals(
            instrId,
            timeFrame,
            breachWindow,
            existingEvents,
            group.settings
        )

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
            }
        }
    }

    fun loadExistingBreaches(): List<BreachEvent> {
        return transaction {
            BreachEvents.select { BreachEvents.eventTimeMs greater System.currentTimeMillis() - 10 * 24 * 3600_000L }
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

