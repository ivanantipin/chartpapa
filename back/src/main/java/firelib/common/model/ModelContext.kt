package firelib.common.model

import firelib.common.config.ModelBacktestConfig
import firelib.common.mddistributor.MarketDataDistributor
import firelib.common.timeservice.TimeService
import firelib.common.tradegate.TradeGate

class ModelContext(val timeService: TimeService,
                   val mdDistributor: MarketDataDistributor,
                   val tradeGate: TradeGate,
                   val instruments: List<String>,
                   val config: ModelBacktestConfig)