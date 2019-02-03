package firelib.common.timeboundscalc

import firelib.common.config.ModelBacktestConfig
import java.time.Instant

/**

 */
class TimeBoundsCalculatorImpl() : TimeBoundsCalculator{

    fun calcStartDate(cfg: ModelBacktestConfig): Instant {
        val startDtGmt = if (cfg.startDateGmt == null) Instant.EPOCH else cfg.startDateGmt

        val readers = cfg.instruments.map {it.fact(startDtGmt)}

        if(cfg.verbose){
            cfg.instruments.forEachIndexed({idx,cfg->
                println("earliest start date for ${cfg} is ${readers[idx].startTime()}")
            })
        }

        val maxReadersStartDate = readers.maxBy {it.current().time().getEpochSecond()}!!.current().time()

        readers.forEach {it.close()}

        return if (maxReadersStartDate.isAfter(startDtGmt)) maxReadersStartDate else startDtGmt

    }

    override fun invoke(cfg : ModelBacktestConfig): Pair<Instant, Instant> {
        val startDt: Instant = calcStartDate(cfg)
        val endDt: Instant = if (cfg.endDate == null) Instant.now() else cfg.endDate
        return Pair(startDt, endDt)
    }
}