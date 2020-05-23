package firelib.finam

import firelib.core.domain.Interval
import java.lang.RuntimeException

enum class Period(val id: Int) {
    // TODO: add more periods
    FIVE_MINUTES(3),
    DAILY(8),
    TEN_MINUTES(4),
    ONE_MINUTES(2);

    companion object{
        fun forInterval(interval: Interval): Period {
            return when {
                interval == Interval.Min10 -> TEN_MINUTES
                interval == Interval.Min1 -> ONE_MINUTES
                else -> throw RuntimeException("not supported ${interval}")
            }
        }

    }
}
