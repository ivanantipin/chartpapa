package firelib.common.tradegate

import firelib.common.agenda.AgendaComponent
import firelib.common.core.{ModelConfigContext, OnContextInited}
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.timeservice.TimeServiceComponent


trait TradeGateStubComponent {
    this : AgendaComponent with TimeServiceComponent with ModelConfigContext with MarketDataDistributorComponent with OnContextInited=>

    var stub: TradeGateStub = null

    var tradeGateDelay : TradeGate = null

    initMethods += (()=>{
        stub = new TradeGateStub(marketDataDistributor, modelConfig, timeService)
        tradeGateDelay = new TradeGateDelay(timeService,modelConfig.networkSimulatedDelayMs,stub,agenda)
    })

}
