package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.misc.UtilsHandy
import firelib.core.flattenAll
import firelib.core.makePositionEqualsTo
import firelib.core.timeseries.TimeSeries
import firelib.core.domain.Ohlc
import firelib.core.domain.ret
import firelib.core.misc.Quantiles
import firelib.core.misc.atMoscow
import firelib.core.report.GenericMapWriter
import firelib.core.store.MdStorageImpl
import firelib.core.store.finamMapperWriter
import java.time.LocalDate
import kotlin.math.abs


class SiStrat(context: ModelContext, val props: Map<String, String>) : Model(context, props) {

    val buffer = mutableListOf<MutableMap<String, Any>>()
    val finalBuffer = mutableListOf<MutableMap<String, Any>>()

    init {
        val intraTs = enableSeries(Interval.Min30, 100, false)[0]
        val dayTs = enableSeries(Interval.Day, interpolated = true)[0]

        dayTs.preRollSubscribe {
            if (dayTs.count() > 2) {
                buffer.forEach { mp ->
                    mp["dow"] = currentTime().minusSeconds(5 * 3600).atMoscow().dayOfWeek.ordinal
                    mp["gap"] = it[0].open - it[1].close
                }
                finalBuffer += buffer
                buffer.clear()
            }
        }


        intraTs.preRollSubscribe {
            val idx = currentTime().atMoscow().toLocalTime().toSecondOfDay() / 1800
            buffer += mutableMapOf(
                "idx" to idx,
                "ret_local" to it[0].ret(),
                "ret" to dayTs[0].ret()
            )
        }

    }

    override fun onBacktestEnd() {
        super.onBacktestEnd()
        println("writitng ${finalBuffer.size}")
        GenericMapWriter.write(context.config.getReportDbFile(), finalBuffer, "si_stat")
    }
}

fun siModel(): ModelBacktestConfig {
    return ModelBacktestConfig(SiStrat::class).apply {
        instruments = listOf("SPFB_Si")
        startDate(LocalDate.now().minusDays(1500))
    }
}

fun main() {
    siModel().runStrat()
}