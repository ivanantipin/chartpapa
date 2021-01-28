package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.indicators.SRMaker
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object HistoricalLevels{

    fun historicalLevels(ticker: InstrId): HistoricalBreaches {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, Interval.Min10, 20000)
        val eventTimeMs = Interval.Min10.truncTime(System.currentTimeMillis())


        return transaction {

            val rr = LevelSensitivityConfig.select { LevelSensitivityConfig.instrId eq ticker.id }.first()
            val hits = rr[LevelSensitivityConfig.hits]
            val ziggy = rr[LevelSensitivityConfig.zigzag_pct]

            val maker = SRMaker(1000, hits, ziggy)

            val be =
                BreachEvents.select { BreachEvents.instrId eq ticker.id and
                        (BreachEvents.timeframe eq TimeFrame.M30.name) and
                        (BreachEvents.eventType eq BreachType.LEVELS_SNAPSHOT.name) and
                        (BreachEvents.eventTimeMs eq eventTimeMs)
                }
                    .firstOrNull()

            if (be == null) {
                val fileName =
                    BreachFinder.makeSnapFileName(BreachType.LEVELS_SNAPSHOT.name, ticker.id, TimeFrame.M30, eventTimeMs)

                targetOhlcs.forEach { maker.addOhlc(it) }

                val bytes = ChartService.drawLevels(maker.currentLevels, targetOhlcs, "Levels for ${ticker.code} ")
                saveFile(bytes, fileName)

                updateDatabase("update levels events", {
                    BreachEvents.insert {
                        it[instrId] = ticker.id
                        it[timeframe] = TimeFrame.M30.name
                        it[BreachEvents.eventTimeMs] = eventTimeMs
                        it[photoFile] = fileName
                        it[eventType] = BreachType.LEVELS_SNAPSHOT.name
                    }
                }).get()
                HistoricalBreaches(filePath = fileName)
            } else {
                HistoricalBreaches(filePath = be[BreachEvents.photoFile])
            }
        }
    }
}