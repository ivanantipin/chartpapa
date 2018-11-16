package firelib.common.timeboundscalc

import java.time.Instant

import firelib.common.config.ModelBacktestConfig
import firelib.common.misc.DateUtils
import firelib.common.reader.ReadersFactoryComponent

/**

 */
trait TimeBoundsCalculatorComponent extends DateUtils{
    this : ReadersFactoryComponent =>

    val timeBoundsCalculator : TimeBoundsCalculator = new TimeBoundsCalculatorImpl()

    class TimeBoundsCalculatorImpl extends TimeBoundsCalculator{

        def calcStartDate(cfg: ModelBacktestConfig): Instant = {
            val startDtGmt = if (cfg.startDateGmt == null) Instant.EPOCH else cfg.startDateGmt.parseTimeStandard

            val readers = cfg.instruments.map(c=>readersFactory.apply(c, startDtGmt))

            val maxReadersStartDate = readers.maxBy(r =>r.current.time.getEpochSecond).current.time

            readers.foreach(_.close())

            return if (maxReadersStartDate.isAfter(startDtGmt)) maxReadersStartDate else startDtGmt

        }

        override def apply(cfg : ModelBacktestConfig): (Instant, Instant) = {
            val startDt: Instant = calcStartDate(cfg)
            val endDt: Instant = if (cfg.endDate == null) Instant.now() else cfg.endDate.parseTimeStandard
            return (startDt, endDt)
        }
    }
}