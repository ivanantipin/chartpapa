package com.firelib.techbot.tdline

import com.firelib.techbot.LineConfig
import com.firelib.techbot.TrendsCreator
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.staticdata.OhlcsService
import com.firelib.techbot.subscriptions.SubscriptionService
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.util.concurrent.Future

object UpdateTrendLinesSensitivities {

    fun updateSensitivities(subscriptionService: SubscriptionService, ohlcsService: OhlcsService) {
        subscriptionService.liveInstruments().forEach { instr ->
            updateSens(instr, ohlcsService).get()
        }
    }

    fun updateSens(ticker: InstrId, ohlcsService: OhlcsService): Future<Unit> {

        return DbHelper.updateDatabase("senses update ${ticker.code}") {
            SensitivityConfig.deleteWhere { SensitivityConfig.instrId eq ticker.id }
            TimeFrame.values().forEach { timeFrame ->
                val targetOhlcs = ohlcsService.getOhlcsForTf(ticker, timeFrame.interval)

                var updated = false
                for (i in 7 downTo 2) {
                    if (updateForOrder(targetOhlcs, ticker, timeFrame, i)) {
                        updated = true
                        break
                    }
                }
                if (!updated) {
                    println("update senses : not found ${ticker}, ${timeFrame}")
                }
            }
        }
    }

    private fun updateForOrder(
        targetOhlcs: List<Ohlc>,
        instr: InstrId,
        tf: TimeFrame,
        pvtOrder: Int
    ): Boolean {
        var lineRSquare = 1.0
        while (lineRSquare > 0.9) {
            val lines = TrendsCreator.findRegresLines(targetOhlcs, LineConfig(pvtOrder, lineRSquare))
            if (lines.size >= 2) {
                println("final for ${instr} ${tf} rsquare is ${lineRSquare} line count is ${lines.size}")
                SensitivityConfig.deleteWhere {
                    SensitivityConfig.instrId eq instr.id and (SensitivityConfig.timeframe eq tf.name)
                }
                SensitivityConfig.insert {
                    it[instrId] = instr.id
                    it[timeframe] = tf.name
                    it[rSquare] = lineRSquare
                    it[pivotOrder] = pvtOrder
                }
                return true
            }
            lineRSquare -= 0.005
        }
        return false
    }

}
