package com.firelib.techbot

import com.firelib.techbot.UpdateLevelsSensitivities.updateLevelSenses
import firelib.core.domain.Interval
import firelib.indicators.SRMaker
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.transactions.transaction


object UpdateLevelsSensitivities{

    fun updateLevelSenses() {
        transaction {

            LevelSensitivityConfig.deleteAll()

            SymbolsDao.available().forEach { instr ->
                val ticker = instr.code
                val targetOhlcs = BotHelper.getOhlcsForTf(ticker, Interval.Min10, 20000)

                val maxPct = 0.05
                val minPct = 0.015

                var zigzag = 0.0
                var hits = 0

                for( i in 100 downTo 0){
                    zigzag = minPct + i*(maxPct - minPct)/100.0
                    hits = (i + 40)/20

                    val maker = SRMaker(1000, hits , zigzag)
                    targetOhlcs.forEach { maker.addOhlc(it) }

                    if(maker.currentLevels.size >= 2){
                        println("found for ticker ${ticker} hits ${hits} zigzag ${zigzag}")
                        LevelSensitivityConfig.insert {
                            it[LevelSensitivityConfig.ticker] = ticker
                            it[LevelSensitivityConfig.hits] = hits
                            it[LevelSensitivityConfig.zigzag_pct] = zigzag
                        }
                        break
                    }
                }
            }
        }
    }




}



suspend fun main(args: Array<String>) {
    initDatabase()
    updateLevelSenses()
}
