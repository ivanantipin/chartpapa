package firelib.common.core

import firelib.common.Trade
import firelib.domain.OrderState

import scala.collection.mutable.ArrayBuffer

class ModelOutput(val modelProps : Map[String,String]){
    val trades = new ArrayBuffer[Trade]()
    val orderStates = new ArrayBuffer[OrderState]()
}
