package firelib.core.domain

import firelib.common.Trade
import firelib.model.Model


class ModelOutput(val model : Model, val modelProps : Map<String,String>){
    val trades = mutableListOf<Trade>()
    val orderStates = mutableListOf<OrderState>()
}