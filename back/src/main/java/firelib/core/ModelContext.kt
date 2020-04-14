package firelib.core

import firelib.core.InstrumentMapper
import firelib.core.TradeGate
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.ModelConfig
import firelib.core.mddistributor.MarketDataDistributor
import firelib.core.timeservice.TimeService

class ModelContext(val timeService: TimeService,
                   val mdDistributor: MarketDataDistributor,
                   val tradeGate: TradeGate,
                   val instrumentMapper : InstrumentMapper,
                   val config: ModelConfig)