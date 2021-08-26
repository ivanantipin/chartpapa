package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
import firelib.core.domain.InstrId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

object BotConfig{
    var pivotOrders: Map<Pair<String, String>, Int> = emptyMap()
    var rSquares: Map<Pair<String, String>, Double> = emptyMap()
    var lineRSquare: Double = 0.995
    var window: Long = 300
    var pivotOrder = 10

    init {
        transaction {
            rSquares = SensitivityConfig.selectAll()
                .associateBy({ Pair(it[SensitivityConfig.instrId], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.rSquare] })

            pivotOrders = SensitivityConfig.selectAll()
                .associateBy({ Pair(it[SensitivityConfig.instrId], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.pivotOrder] })
        }
    }

    fun getConf(ticker: InstrId, timeFrame: TimeFrame) : LineConfig {
        return LineConfig(getPivot(ticker, timeFrame), getRSquare(ticker, timeFrame))
    }

    fun getRSquare(ticker : InstrId, timeFrame : TimeFrame) : Double{
        return rSquares.getOrDefault(Pair(ticker.id, timeFrame.name), lineRSquare)
    }

    fun getPivot(ticker : InstrId, timeFrame : TimeFrame) : Int{
        return pivotOrders.getOrDefault(Pair(ticker.id, timeFrame.name), pivotOrder)
    }



}