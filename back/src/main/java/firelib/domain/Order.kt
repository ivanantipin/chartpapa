package firelib.common

import com.funstat.domain.InstrId
import firelib.common.misc.DurableChannel
import firelib.common.misc.dbl2Str
import firelib.common.misc.toStandardString
import firelib.domain.OrderState
import firelib.domain.OrderType
import firelib.domain.Side
import java.time.Instant

data class Order(val orderType: OrderType,
                 val price: Double,
                 val qtyLots: Int,
                 val side: Side,
                 val security : String,
                 val id: String,
                 val placementTime: Instant,
                 val instr : InstrId

){

    val tradeSubscription: DurableChannel<Trade> = DurableChannel()
    val orderSubscription: DurableChannel<OrderState> = DurableChannel()

    init {
        require(qtyLots > 0) {"order qty <= 0!!"}
        require(price > 0 || orderType == OrderType.Market) {"price : $price <=  0!!"}
    }


    val anyInfo = HashMap<String,Any>(0)

    val longPrice = (price *1000000).toLong()

    override fun toString(): String = "Order(price=${dbl2Str(price,6)} qty=$qtyLots side=$side type=$orderType orderId=$id sec=$security time=${placementTime.toStandardString()}})"
}

fun Order.reject(reason : String){
    this.orderSubscription.publish(OrderState(this, OrderStatus.Rejected, Instant.now(), reason))
}

fun Order.cancelReject(reason : String){
    this.orderSubscription.publish(OrderState(this, OrderStatus.CancelFailed, Instant.now(), reason))
}


fun Order.cancel(){
    this.orderSubscription.publish(OrderState(this, OrderStatus.Cancelled, Instant.now(), ""))
}

fun Order.done(){
    this.orderSubscription.publish(OrderState(this, OrderStatus.Done, Instant.now(), ""))
}
fun Order.accepted(){
    this.orderSubscription.publish(OrderState(this, OrderStatus.Accepted, Instant.now(), ""))
}