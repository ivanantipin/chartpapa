package firelib.model

import firelib.core.config.ModelBacktestConfig
import firelib.core.InstrumentMapper
import firelib.core.mddistributor.MarketDataDistributor
import firelib.core.timeservice.TimeService
import firelib.core.TradeGate

class ModelContext(val timeService: TimeService,
                   val mdDistributor: MarketDataDistributor,
                   val tradeGate: TradeGate,
                   val instrumentMapper : InstrumentMapper,
                   val config: ModelBacktestConfig)