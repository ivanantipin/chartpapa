package chart

import com.firelib.techbot.*
import com.firelib.techbot.chart.ChartService
import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.*
import firelib.core.misc.atMoscow
import firelib.core.store.GlobalConstants
import firelib.finam.timeFormatter
import firelib.indicators.SR
import firelib.indicators.SRMaker
import org.jetbrains.exposed.sql.select
import java.time.Instant
import kotlin.math.sign


enum class BreachType {
    TREND_LINE, DEMARK_SIGNAL, TREND_LINE_SNAPSHOT, LEVELS_SNAPSHOT, LEVELS_SIGNAL
}

object BreachFinder {

    fun makeSnapFileName(prefix: String, ticker: String, timeFrame: TimeFrame, eventTimeMs: Long): String {
        val fileName = "${prefix}_${ticker}_${timeFrame}_$eventTimeMs"
        return GlobalConstants.imgFolder.resolve("${fileName}.png").toFile().absoluteFile.toString()
    }

    fun findNewBreaches(
        ticker: InstrId,
        timeFrame: TimeFrame,
        breachWindow: Int,
        existingBreaches: Set<BreachEventKey>
    ): List<BreachEvent> {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, timeFrame.interval)
        val conf = BotConfig.getConf(ticker, timeFrame)
        val lines = TrendsCreator.findRegresLines(targetOhlcs, conf)


        return lines.filter { it.intersectPoint != null && it.intersectPoint!!.first >= targetOhlcs.size - breachWindow }
            .groupBy {
                val endTime = targetOhlcs[it.intersectPoint!!.first].endTime
                BreachEventKey(ticker.codeAndExch(), timeFrame, endTime.toEpochMilli(), BreachType.TREND_LINE)
            }
            .filter { !existingBreaches.contains(it.key) }
            .map {
                val key = it.key
                println(key)
                val fileName = makeSnapFileName(
                    BreachType.TREND_LINE.name,
                    ticker.codeAndExch(),
                    timeFrame,
                    it.key.eventTimeMs
                )

                val time = timeFormatter.format(Instant.ofEpochMilli(it.key.eventTimeMs).atMoscow())
                val title = "Breakout for ${ticker} (${timeFrame}, time is ${time} msk)"
                val img = ChartService.drawLines(it.value, targetOhlcs, title)
                saveFile(img, fileName)
                BreachEvent(key, fileName)
            }
    }

    fun findLevelBreaches(
        ticker: InstrId,
        timeFrame: TimeFrame,
        breachWindow: Int,
        existingBreaches: Set<BreachEventKey>
    ): List<BreachEvent> {
        val targetOhlcs = BotHelper.getOhlcsForTf(ticker, Interval.Min10, 20000)
        val levels = makeLevels(ticker, targetOhlcs)

        return levels.flatMap { sr ->
            val breachDetected =
                sign(targetOhlcs.at(-1).close - sr.level) == sign(sr.level - targetOhlcs.at(-breachWindow).close)
            val time = targetOhlcs.last().endTime.toEpochMilli()
            val key = BreachEventKey(ticker.codeAndExch(), timeFrame, time, BreachType.LEVELS_SIGNAL)

            if (breachDetected && !existingBreaches.contains(key)) {
                val side = if (targetOhlcs.at(-1).close - sr.level > 0) Side.Buy else Side.Sell

                val fileName = makeSnapFileName(
                    BreachType.LEVELS_SIGNAL.name,
                    ticker.codeAndExch(),
                    timeFrame,
                    time
                )

                val timeStr = timeFormatter.format(targetOhlcs.at(-1).endTime.atMoscow())
                val title = "Level breakout for ${ticker} (${timeFrame}, time is ${timeStr} msk)"

                val img =
                    ChartService.drawLevelsBreaches(listOf(LevelSignal(side, time, sr)), targetOhlcs, title)

                saveFile(img, fileName)

                listOf(BreachEvent(key, fileName))
            } else {
                emptyList()
            }
        }
    }

    private fun makeLevels(
        ticker: InstrId,
        targetOhlcs: List<Ohlc>
    ): List<SR> {

        val lst = LevelSensitivityConfig.select { LevelSensitivityConfig.codeAndExch eq ticker.codeAndExch() }.toList()
        if (lst.isEmpty()) {
            println("no level senses for ticker ${ticker}")
            return emptyList()
        }


        val rr = lst.first()
        val hits = rr[LevelSensitivityConfig.hits]
        val ziggy = rr[LevelSensitivityConfig.zigzag_pct]

        val maker = SRMaker(1000, hits, ziggy)

        targetOhlcs.forEach { maker.addOhlc(it) }
        return maker.currentLevels
    }
}


fun List<Ohlc>.at(idx: Int): Ohlc {
    if (idx < 0) {
        return this[this.size + idx]
    }
    return this[idx]
}

fun main() {
    initDatabase()
    BreachFinder.findNewBreaches(InstrId(code ="RASP", market = "1"), TimeFrame.H, 5, emptySet())
    //UpdateLevelsSensitivities.updateLevelSenses()

}
