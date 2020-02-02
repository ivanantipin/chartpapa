package firelib.core

import firelib.common.Trade
import firelib.common.model.Model
import firelib.core.domain.OrderState


class ModelOutput(val model : Model, val modelProps : Map<String,String>){
    val trades = mutableListOf<Trade>()
    val orderStates = mutableListOf<OrderState>()
}