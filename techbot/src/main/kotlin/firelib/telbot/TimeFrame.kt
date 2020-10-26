package firelib.telbot

import firelib.core.domain.Interval

enum class TimeFrame(val interval : Interval) {
    H(Interval.Min60), D(Interval.Day)
}