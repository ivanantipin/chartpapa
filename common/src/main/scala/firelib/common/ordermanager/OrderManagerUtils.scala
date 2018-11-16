package firelib.common.ordermanager

import firelib.common.{Order, OrderType, Side}

trait OrderManagerUtils {
    this : OrderManager =>

    def managePosTo(pos: Int): Unit = {
        if(hasPendingState()){
            return
        }
        getOrderForDiff(position, pos) match {
            case Some(ord) => submitOrders(ord)
            case _ =>
        }
    }

    def buyAtLimit(price: Double, vol: Int = 1) = {
        submitOrders(new Order(OrderType.Limit, price, vol, Side.Buy, security, nextOrderId, currentTime))
    }

    def sellAtLimit(price: Double, vol: Int = 1) = {
        submitOrders(new Order(OrderType.Limit, price, vol, Side.Sell, security, nextOrderId, currentTime))
    }

    def buyAtStop(price: Double, vol: Int = 1) = {
        submitOrders(new Order(OrderType.Stop, price, vol, Side.Buy, security, nextOrderId, currentTime))
    }

    def sellAtStop(price: Double, vol: Int = 1) = {
        submitOrders(new Order(OrderType.Stop, price, vol, Side.Sell, security, nextOrderId, currentTime))
    }

    def getOrderForDiff(currentPosition: Int, targetPos: Int): Option[Order] = {
        val vol = targetPos - currentPosition
        if (vol != 0) {
            return Some(new Order(OrderType.Market, 0, math.abs(vol), if (vol > 0) Side.Buy else Side.Sell, security, nextOrderId, currentTime))
        }
        return None
    }

    def flattenAll(reason: Option[String] = None) = {
        cancelAllOrders()
        managePosTo(0)
    }

    def cancelAllOrders() = {cancelOrders(liveOrders.filter(o=>o.orderType != OrderType.Market) :_*)}

}
