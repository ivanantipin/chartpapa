package firelib.model

import firelib.core.backtest.Backtester
import firelib.model.prod.ProfileModel
import firelib.model.prod.RealDivModel
import firelib.model.prod.TrendModel
import firelib.model.prod.VolatilityBreak


fun main() {

    val pcfg = RealDivModel.modelConfig(250_000)
    pcfg.runConfig.maxRiskMoney = 2_300_000
    pcfg.runConfig.maxRiskMoneyPerSec = 300_000
    Backtester.runSimple(
        listOf(
            pcfg,
            TrendModel.modelConfig(250_000),
            VolatilityBreak.modelConfig(250_000)
        )
    )
}