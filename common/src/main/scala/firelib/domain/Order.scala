package firelib.common

import java.time.Instant

import firelib.common.misc.dateUtils._
import firelib.common.misc.utils
case class Order(val orderType: OrderType, val price: Double, val qty: Int, val side: Side, val security : String, val id: String, val placementTime: Instant){
    
    assert(qty > 0, "order qty <= 0!!")
    assert(price > 0 || orderType == OrderType.Market, s"price : $price <=  0!!")
    assert(id != null, "id is null!!")
    assert(security != null, "security is null!!")
    assert(placementTime != null, "placement time is null!!")

    val longPrice = (price *1000000).toLong

    override def toString: String = s"Order(price=${utils.dbl2Str(price,6)} qty=$qty side=$side type=$orderType orderId=$id sec=$security time=${placementTime.toStandardString}})"
}


