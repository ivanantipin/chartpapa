package com.firelib.techbot

import com.firelib.techbot.BotHelper.getOhlcsForTf
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.Ohlc
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


object UpdateSensitivities{

    fun main(args: Array<String>) {
        initDatabase()
        updateSensitivties()
    }

    fun updateSensitivties() {
        updateDatabase("update sensitivities") {

            SensitivityConfig.deleteAll()

            SymbolsDao.available().forEach { instr ->
                val ticker = instr.code
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
        }.get()
    }


    private fun updateForOrder(
        targetOhlcs: List<Ohlc>,
        ticker: String,
        tf: TimeFrame,
        pvtOrder: Int
    ): Boolean {
        var lineRSquare = 1.0
        while (lineRSquare > 0.9) {
            val lines = TrendsCreator.findRegresLines(targetOhlcs, LineConfig(pvtOrder, lineRSquare))
            if (lines.size >= 2) {
                println("final for ${ticker} ${tf} rsquare is ${lineRSquare} line count is ${lines.size}")
                SensitivityConfig.deleteWhere {
                    SensitivityConfig.ticker eq ticker and (SensitivityConfig.timeframe eq tf.name)
                }
                SensitivityConfig.insert {
                    it[SensitivityConfig.ticker] = ticker
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



