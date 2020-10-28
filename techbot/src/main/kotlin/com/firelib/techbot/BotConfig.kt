package com.firelib.techbot

import com.firelib.techbot.domain.TimeFrame
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
                .associateBy({ Pair(it[SensitivityConfig.ticker], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.rSquare] })

            pivotOrders = SensitivityConfig.selectAll()
                .associateBy({ Pair(it[SensitivityConfig.ticker], it[SensitivityConfig.timeframe]) },
                    { it[SensitivityConfig.pivotOrder] })
        }
    }

    fun getConf(ticker: String, timeFrame: TimeFrame) : LineConfig {
        val ret = LineConfig(getPivot(ticker, timeFrame), getRSquare(ticker, timeFrame))
        println("returning config for ${ticker} frame ${timeFrame} : ${ret}")
        return ret
    }

    fun getRSquare(ticker : String, timeFrame : TimeFrame) : Double{
        return rSquares.getOrDefault(Pair(ticker, timeFrame.name), lineRSquare)
    }

    fun getPivot(ticker : String, timeFrame : TimeFrame) : Int{
        return pivotOrders.getOrDefault(Pair(ticker, timeFrame.name), pivotOrder)
    }



}