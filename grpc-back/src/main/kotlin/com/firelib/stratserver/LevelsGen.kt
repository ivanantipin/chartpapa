package com.firelib.stratserver

import com.firelib.DatePoint
import com.firelib.Level
import com.firelib.Levels
import com.firelib.OhlcPeriod
import com.funstat.sequenta.Sequenta
import com.funstat.sequenta.SignalType
import firelib.common.interval.Interval
import firelib.common.misc.toInstantDefault
import firelib.domain.Ohlc
import org.joda.time.PeriodType
import java.time.Period
import java.util.*

class LevelsGen(val broadcaster: Broadcaster<Levels>, val ticker: String) {

    private class IntervalState {
        val sequenta = Sequenta()
        val levels = LinkedList<DatePoint>()

        fun onOhlc(ohlc: Ohlc) : Boolean{
            var ret = false
            sequenta.onOhlc(ohlc)
                    .filter { it.type == SignalType.SetupReach }
                    .forEach {
                        ret = true
                        levels += DatePoint.newBuilder().setLabel("")
                                .setTimestamp(it.reference.getStart().toInstantDefault().toEpochMilli())
                                .setValue(it.reference.tdst).build()
                        if (levels.size > 10) {
                            levels.removeFirst()
                        }
                    }
            return ret
        }
    }

    private val dayState = IntervalState()
    private val weekState = IntervalState()


    val defferer = Defferer()


    @Synchronized
    fun onOhlc(ohlc: Ohlc, interval: Interval) {
        val state = if (interval == Interval.Day) dayState else weekState;
        if(state.onOhlc(ohlc)){
            defferer.executeLater { updateLevels() }
        }
    }

    @Synchronized
    private fun updateLevels() {
        broadcaster.add(Levels.newBuilder()
                .setTicker(ticker)
                .addAllLevels(mapLevels(weekState.levels, OhlcPeriod.Week))
                .addAllLevels(mapLevels(dayState.levels, OhlcPeriod.Day))
                .build());
    }

    private fun mapLevels(levels: List<DatePoint>, lvlType: OhlcPeriod): List<Level> {
        return levels.map { lvl ->
            Level.newBuilder()
                    .apply {
                        level = DatePoint.newBuilder().apply {
                            timestamp = lvl.timestamp;
                            value = lvl.value
                        }.build()
                        levelType = lvlType
                    }.build()
        }
    }

}