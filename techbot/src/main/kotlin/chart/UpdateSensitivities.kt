package com.firelib.techbot

import com.firelib.techbot.BotHelper.getOhlcsForTf
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import firelib.core.domain.Ohlc
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import java.util.concurrent.Future


object UpdateSensitivities{

    fun updateSensitivties() {
        MdService.liveSymbols.forEach { instr ->
            updateSens(instr).get()
        }
    }

    fun updateSens(ticker: InstrId): Future<Unit> {

        return updateDatabase("senses update ${ticker}") {
            SensitivityConfig.deleteWhere { SensitivityConfig.codeAndExch eq ticker.codeAndExch() }
            TimeFrame.values().forEach { timeFrame ->
                val targetOhlcs = getOhlcsForTf(ticker, timeFrame.interval)

                var updated = false
                for (i in 7 downTo 2) {
                    if (updateForOrder(targetOhlcs, ticker, timeFrame, i)) {
                        updated = true
                        break
                    }
                }
                if (!updated) {
                    println("not found ${ticker}, ${timeFrame}")
                }
            }
        }

    }


    private fun updateForOrder(
        targetOhlcs: List<Ohlc>,
        ticker: InstrId,
        tf: TimeFrame,
        pvtOrder: Int
    ): Boolean {
        var lineRSquare = 1.0
        while (lineRSquare > 0.9) {
            val lines = TrendsCreator.findRegresLines(targetOhlcs, LineConfig(pvtOrder, lineRSquare))
            if (lines.size >= 2) {
                println("final for ${ticker} ${tf} rsquare is ${lineRSquare} line count is ${lines.size}")
                SensitivityConfig.deleteWhere {
                    SensitivityConfig.codeAndExch eq ticker.codeAndExch() and (SensitivityConfig.timeframe eq tf.name)
                }
                SensitivityConfig.insert {
                    it[SensitivityConfig.codeAndExch] = ticker.codeAndExch()
                    it[SensitivityConfig.timeframe] = tf.name
                    it[rSquare] = lineRSquare
                    it[SensitivityConfig.pivotOrder] = pvtOrder
                }
                return true
            }
            lineRSquare -= 0.005
        }
        return false
    }
}

fun main() {
    initDatabase()
    UpdateSensitivities.updateSensitivties()
}
