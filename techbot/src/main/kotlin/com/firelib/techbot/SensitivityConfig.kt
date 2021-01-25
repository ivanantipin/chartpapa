package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import org.jetbrains.exposed.sql.Table

object SensitivityConfig: Table() {
    val codeAndExch = varchar("ticker", 10)
    val pivotOrder = integer("pivot_order")
    val rSquare = double("r_square")
    val timeframe = varchar("timeframe", 10).default(TimeFrame.D.name)

    override val primaryKey = PrimaryKey(codeAndExch, timeframe, name = "sens_conf_pk")
}

object LevelSensitivityConfig: Table() {
    val codeAndExch = varchar("ticker", 10)
    val hits = integer("hits")
    val zigzag_pct = double("zigzag_pct")
    override val primaryKey = PrimaryKey(codeAndExch, name = "level_sens_conf_pk")
}