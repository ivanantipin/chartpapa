package com.firelib.techbot.domain

import firelib.core.domain.Interval

enum class TimeFrame(val interval: Interval) {
    H(Interval.Min60), D(Interval.Day), H4(Interval.Min240), M30(Interval.Min30), W(Interval.Week), All(Interval.None)
}