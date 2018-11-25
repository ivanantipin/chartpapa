package firelib.common.report

import firelib.common.Order
import firelib.common.Side
import firelib.common.misc.dbl2Str
import firelib.common.misc.toStandardString


class OrderSerializer : ReportConsts {

    companion object {
        fun serializeOrder(order: Order): List<String> = orderColsDef.map({ it.second }).map({ it(order) })

        fun serialize(order: Order): List<String> {
            return serializeOrder(order)
        }

        fun makeMetric(name: String, funct: (Order) -> String): Pair<String, (Order) -> String> {
            return Pair(name, funct)
        }

        val orderColsDef = arrayOf(
                makeMetric("Ticker", { it.security }),
                makeMetric("OrderId", { it.id }),
                makeMetric("OrderType", { it.orderType.name }),
                makeMetric("BuySell", { if (it.side == Side.Buy) "1" else "-1" }),
                makeMetric("EntryDate", { it.placementTime.toStandardString() }),
                makeMetric("Price", { dbl2Str(it.price, ReportConsts.decPlaces) }),
                makeMetric("Qty", { it.qty.toString() })
        )

        fun getHeader(): List<String> {
            return orderColsDef.map({ it.first })
        }

    }

}


