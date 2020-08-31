package firelib.model

import firelib.core.backtest.Backtester
import firelib.model.prod.*


fun main() {

    val pcfg = RealDivModel.modelConfig(250_000)
    val runConfig = commonRunConfig()
    runConfig.maxRiskMoney = 2_300_000
    runConfig.maxRiskMoneyPerSec = 300_000
    Backtester.runSimple(
        listOf(
            pcfg,
            TrendModel.modelConfig(250_000),
            VolatilityBreak.modelConfig(250_000)
        ), runConfig
    )
}