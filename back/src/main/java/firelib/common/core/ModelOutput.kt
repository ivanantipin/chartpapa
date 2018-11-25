package firelib.common.core

import firelib.common.Trade
import firelib.domain.OrderState


class ModelOutput(val modelProps : Map<String,String>){
    val trades = ArrayList<Trade>()
    val orderStates = ArrayList<OrderState>()
}