package firelib.common.core.report

import firelib.common.Order
import firelib.domain.Side

val orderColsDefs: Array<ColDef<Order, out Any>> = arrayOf(
        makeMetric("Ticker", { it.security }),
        makeMetric("OrderId", { it.id }),
        makeMetric("OrderType", { it.orderType.name }),
        makeMetric("BuySell", { if (it.side == Side.Buy) "1" else "-1" }),
        makeMetric("EntryDate", { it.placementTime }),
        makeMetric("Price", { it.price }),
        makeMetric("Qty", { it.qtyLots })
)