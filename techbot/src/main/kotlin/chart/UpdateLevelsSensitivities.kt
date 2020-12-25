package com.firelib.techbot

import com.firelib.techbot.UpdateLevelsSensitivities.updateLevelSenses
import firelib.core.domain.Interval
import firelib.indicators.SRMaker
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import java.util.concurrent.Future


object UpdateLevelsSensitivities {

    fun updateTicker(ticker: String): Future<Unit> {
        return updateDatabase("levels sens update ${ticker}") {
            LevelSensitivityConfig.deleteWhere { LevelSensitivityConfig.ticker eq ticker }

            val targetOhlcs = BotHelper.getOhlcsForTf(ticker, Interval.Min10, 20000)

            val maxPct = 0.05
            val minPct = 0.015

            var zigzag = 0.0
            var hits = 0

            for (i in 100 downTo 0) {
                zigzag = minPct + i * (maxPct - minPct) / 100.0
                hits = (i + 40) / 20

                val maker = SRMaker(1000, hits, zigzag)
                targetOhlcs.forEach { maker.addOhlc(it) }

                if (maker.currentLevels.size >= 4) {
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

    fun updateLevelSenses() {
        Subscriptions.selectAll()
            .map { Pair(it[Subscriptions.ticker], it[Subscriptions.market]) }
            .distinct()
            .forEach { instr ->
                updateTicker(instr.first).get()
            }
    }
}


suspend fun main(args: Array<String>) {
    initDatabase()
    updateLevelSenses()
}
