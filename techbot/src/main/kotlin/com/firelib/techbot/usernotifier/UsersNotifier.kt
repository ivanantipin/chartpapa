package com.firelib.techbot.usernotifier

import com.firelib.techbot.ConfigParameters
import com.firelib.techbot.Misc
import com.firelib.techbot.SignalType
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.breachevent.BreachEvent
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.breachevent.BreachType
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.DbHelper.getNotifyGroups
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.Executors

class UsersNotifier(val techBotApp: TechbotApp) {

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    fun start() {
        Thread({
            timeSequence(Instant.now(), Interval.Min30, 10000).forEach {
                val enabled = ConfigParameters.NOTIFICATIONS_ENABLED.get() == "true"
                if (!enabled) {
                    log.info("Notifications are disabled")
                } else {
                    try {
                        Misc.measureAndLogTime("signal checking", {
                            check(2)
                        })
                    } catch (e: Exception) {
                        log.error("error in user notifier", e)
                    }
                }
            }
        }, "breach_notifier").start()
    }

    val notifyExecutor = Executors.newFixedThreadPool(40)

    fun check(breachWindow: Int) {
        val existingEvents = loadExistingBreaches().map { it.key }.toSet()
        val notifyGroups = getNotifyGroups()




        log.info("notify group count is ${notifyGroups.size}")
        notifyGroups.map { (group, users) ->
            notifyExecutor.submit {
                try {
                    Misc.measureAndLogTime("group ${group} processing took", {
                        processGroup(group, breachWindow, existingEvents, users)
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
        users: List<UserId>
    ) {
        val instrId = group.instrumentId
        val timeFrame = group.timeFrame

        val instr = techBotApp.instrumentsService().id2inst[instrId]!!

        val breaches = group.signalType.signalGenerator.checkSignals(
            instr,
            timeFrame,
            breachWindow,
            existingEvents,
            group.settings,
            techBotApp
        )

        breaches.forEach {
            notify(it, users)
        }

        if (breaches.isNotEmpty()) {
            DbHelper.updateDatabase("insert breaches ${breaches.size}") {
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

    fun notify(be: BreachEvent, users: List<UserId>) {
        techBotApp.botInterface().sendBreachEvent(be, users)
    }
}

