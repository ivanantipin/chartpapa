package firelib.common.core

import firelib.common.Trade
import firelib.domain.OrderState


class ModelOutput(val modelProps : Map<String,String>){
    val trades = mutableListOf<Trade>()
    val orderStates = mutableListOf<OrderState>()
}