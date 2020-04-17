package firelib.model

import firelib.core.backtest.Backtester
import firelib.model.prod.ReverseModel
import firelib.model.prod.TrendModel


fun main() {
    Backtester.runSimple(
        listOf(
            ProfileModel.modelConfig(100_000),
            TrendModel.modelConfig(100_000),
            ReverseModel.modelConfig(100_000))
    )
}