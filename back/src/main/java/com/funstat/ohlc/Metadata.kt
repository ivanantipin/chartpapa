package com.funstat.ohlc

import com.funstat.domain.InstrId

data class Metadata(val instrIds: List<InstrId>, val period: Int)