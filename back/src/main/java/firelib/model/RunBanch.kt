package firelib.model

import firelib.core.backtest.Backtester
import firelib.model.prod.ProfileModel
import firelib.model.prod.TrendModel
import firelib.model.prod.VolatilityBreak


fun main() {
    Backtester.runSimple(
        listOf(
            ProfileModel.modelConfig(500_000),
            TrendModel.modelConfig(100_000),
//            ReverseModel.modelConfig(500_000),
            VolatilityBreak.modelConfig(100_000)
        )
    )
}