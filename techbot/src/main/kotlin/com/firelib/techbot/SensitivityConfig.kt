package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import org.jetbrains.exposed.sql.Table

object SensitivityConfig: Table() {
    val ticker = varchar("ticker", 10)
    val pivotOrder = integer("pivot_order")
    val rSquare = double("r_square")
    val timeframe = varchar("timeframe", 10).default(TimeFrame.D.name)

    override val primaryKey = PrimaryKey(ticker, timeframe, name = "sens_conf_pk")
}