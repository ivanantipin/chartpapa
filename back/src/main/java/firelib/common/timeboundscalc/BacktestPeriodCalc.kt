package firelib.common.timeboundscalc

import firelib.common.config.ModelBacktestConfig
import java.time.Instant


object BacktestPeriodCalc {

    fun calcStartDate(cfg: ModelBacktestConfig): Instant {

        val readers = cfg.instruments.map {it.factory(cfg.startDateGmt)}

        if(cfg.verbose){
            cfg.instruments.forEachIndexed({idx,cfg->
                println("earliest start date for ${cfg} is ${readers[idx].startTime()}")
            })
        }

        val maxReadersStartDate = readers.maxBy {it.startTime()}!!.current().endTime

        readers.forEach {it.close()}

        return if (maxReadersStartDate.isAfter(cfg.startDateGmt)) maxReadersStartDate else cfg.startDateGmt

    }

    fun calcBounds(cfg : ModelBacktestConfig): Pair<Instant, Instant> {
        return Pair(calcStartDate(cfg), cfg.endDate)
    }
}