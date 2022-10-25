package com.firelib.techbot.usernotifier

import com.firelib.techbot.BotInterface
import com.firelib.techbot.ConfigParameters
import com.firelib.techbot.Misc
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.IChartService
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.DbHelper.getNotifyGroups
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import org.jetbrains.exposed.sql.batchInsert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.concurrent.Executors

class UsersNotifier(val botInterface: BotInterface,
                    val ohlcsService: OhlcsService,
                    val instrumentsService: InstrumentsService,
                    val chartService : IChartService) {

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

    val notifyExecutor = Executors.newFixedThreadPool(40){
        Thread(it).apply { isDaemon=true }
    }

    fun check(breachWindow: Int) {

        val map = DbHelper.getLatestBreachEvents().associateBy({
            NotifyGroup(it.instrId, it.type, it.tf, emptyMap())

        }, {
            Instant.ofEpochMilli(it.eventTimeMs)
        })

        val notifyGroups = getNotifyGroups()

        log.info("notify group count is ${notifyGroups.size}")
        notifyGroups.map { (group, users) ->
            notifyExecutor.submit {
                try {
                    Misc.measureAndLogTime("group ${group} processing took", {
                        processGroup(group, breachWindow, map.getOrDefault(group.copy(settings = emptyMap()), Instant.EPOCH), users)
                    })
                } catch (e: Exception) {
                    log.error("error notifying group ${group}", e)
                }
            }
        }.forEach { it.get() }
    }

    fun processGroup(
        group: NotifyGroup,
        breachWindow: Int,
        lastEvent : Instant,
        users: List<UserId>
    ) {
        val instrId = group.instrumentId
        val timeFrame = group.timeFrame

        val instr = instrumentsService.id2inst[instrId]!!

        val ohlcs = ohlcsService.getOhlcsForTf(instr, timeFrame.interval)

        var threshold = ohlcs[ohlcs.size - breachWindow].endTime
        if(threshold < lastEvent){
           threshold = lastEvent
        }

        val breaches = group.signalType.signalGenerator.checkSignals(
            instr,
            timeFrame,
            threshold,
            group.settings,
            ohlcs
        )

        val bes = breaches.filter { it.first > threshold }.map {
            botInterface.sendBreachEvent(chartService.post(it.second), users)
            it.first
        }

        if (bes.isNotEmpty()) {
            DbHelper.updateDatabase("insert breaches ${breaches.size}") {
                BreachEvents.batchInsert(bes) {
                    this[BreachEvents.instrId] = group.instrumentId
                    this[BreachEvents.timeframe] = group.timeFrame.name
                    this[BreachEvents.eventTimeMs] = it.toEpochMilli()
                    this[BreachEvents.eventType] = group.signalType.name
                }
            }.get()
        }
    }

}

