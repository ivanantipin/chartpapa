package firelib.common.core

import firelib.common.ModelInitResult
import firelib.common.interval.IntervalServiceComponent
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.misc.{Channel, NonDurableChannel, SubChannel}
import firelib.common.model.{BasketModel, Model}
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.timeservice.TimeServiceComponent
import firelib.common.tradegate.TradeGateComponent

import scala.collection.mutable.ArrayBuffer

trait BindModelComponent {

    this: ModelConfigContext
      with MarketDataDistributorComponent
      with TimeServiceComponent
      with TradeGateComponent
      with IntervalServiceComponent=>


    val onModelBinded : SubChannel[Model] = new NonDurableChannel[Model]

    val bindedModels = new ArrayBuffer[Model]

    def bindModelForParams(params: Map[String, String]): Model = {
        val stubs = modelConfig.instruments.map(ins => new OrderManagerImpl(this, ins.ticker))
        val model = modelConfig.newModelInstance().asInstanceOf[BasketModel]
        model.orderManagersFld = stubs.toArray
        model.bindComp = this
        if (model.initModel(params) == ModelInitResult.Success) {
            bindedModels += model
            onModelBinded.asInstanceOf[Channel[Model]].publish(model)
        }
        model
    }

}
