package com.firelib.techbot.usernotifier

import com.firelib.techbot.BotInterface
import com.firelib.techbot.ConfigParameters
import com.firelib.techbot.Misc
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.chart.IChartService
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.marketdata.OhlcsService
import com.firelib.techbot.menu.chatId
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.persistence.DbHelper.getNotifyGroups
import com.firelib.techbot.staticdata.InstrumentsService
import firelib.core.domain.Interval
import firelib.core.misc.timeSequence
import firelib.core.misc.toInstantDefault
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.batchInsert
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant
import java.time.LocalDateTime

class UsersNotifier(val botInterface: BotInterface,
                    val ohlcsService: OhlcsService,
                    val instrumentsService: InstrumentsService,
                    val chartService : IChartService) {

    val log: Logger = LoggerFactory.getLogger(UsersNotifier::class.java)

    val notifierScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    val notificationEnabled = ConfigParameters.NOTIFICATIONS_ENABLED.get() == "true"

    fun start() {
        notifierScope.launch {
            timeSequence(Instant.now(), Interval.Min5, 0, {
                Misc.measureAndLogTime("signal checking", {
                    checkSignals(2)
                })
            })
        }
    }

    suspend fun checkSignals(breachWindow: Int) {

        //fixme does not count settings
        val map = DbHelper.getLatestBreachEvents().associateBy({
            NotifyGroup(it.instrId, it.type, it.tf, emptyMap())

        }, {
            Instant.ofEpochMilli(it.eventTimeMs)
        })

        val notifyGroups = getNotifyGroups()

        log.info("notify group count is ${notifyGroups.size}")

        notifyGroups.map { (group, users) ->
            notifierScope.launch {
                try {
                    Misc.measureAndLogTime("group ${group} processing took", {
                        processGroup(group, breachWindow, map.getOrDefault(group.copy(settings = emptyMap()), Instant.EPOCH), users)
                    })
                } catch (e: Exception) {
                    log.error("error notifying group ${group}", e)
                }
            }
        }.forEach { it.join() }

    }

    suspend fun processGroup(
        group: NotifyGroup,
        breachWindow: Int,
        lastEvent : Instant,
        users: List<UserId>
    ) {
        val instrId = group.instrumentId
        val timeFrame = group.timeFrame

        if(!instrumentsService.id2inst.containsKey(instrId)){
            log.info("No instrument found for id ${instrId} deleting")
            instrumentsService.instrIdDao.deleteById(instrId)
            return
        }

        val instr = instrumentsService.id2inst[instrId]!!

        val ohlcs = ohlcsService.getOhlcsForTf(instr, timeFrame.interval)

        if(ohlcs.isEmpty() || ohlcs.size < breachWindow){
            log.info("No market data for group ${group}")
            return
        }

        val lastMdTime = ohlcs[ohlcs.size - breachWindow].endTime

        val yetAnotherThreshold = LocalDateTime.now().minusDays(20).toInstantDefault()

        if(lastMdTime < yetAnotherThreshold){
            log.info("market data is stale, not notifying group ${group}")
            return
        }

        var threshold = lastMdTime
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



        val bes = breaches.filter { it.first > threshold}.map {
            if(notificationEnabled){
                botInterface.sendPhoto(chartService.post(it.second), users, "#${instr.code}_Signal")
            }
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
            }
        }
    }

}

