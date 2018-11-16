package firelib.common.core

import firelib.common.agenda.AgendaComponent
import firelib.common.config.ModelBacktestConfig
import firelib.common.interval.IntervalServiceComponent
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.reader.ReadersFactoryComponent
import firelib.common.report.OhlcReportWriterComponent
import firelib.common.timeboundscalc.TimeBoundsCalculatorComponent
import firelib.common.timeservice.{TimeServiceComponent, TimeServiceManagedComponent}
import firelib.common.tradegate.{TradeGateComponent, TradeGateStubComponent}


class SimpleRunCtx(val modelConfig: ModelBacktestConfig) extends  OnContextInited
with BacktestComponent
with TimeBoundsCalculatorComponent
with ModelConfigContext
with ReadersFactoryComponent
with MarketDataDistributorComponent
with IntervalServiceComponent
with BindModelComponent
with TimeServiceComponent
with AgendaComponent
with TradeGateComponent
with TradeGateStubComponent
with TimeServiceManagedComponent
with OhlcReportWriterComponent{

    timeService = timeServiceManaged

    initMethods +=  (()=>{
        tradeGate = tradeGateDelay
    })

}


