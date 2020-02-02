package firelib.core.report

import firelib.common.Order
import firelib.core.domain.Side
import firelib.core.report.dao.ColDef

val orderColsDefs: Array<ColDef<Order, out Any>> = arrayOf(
        makeMetric("Ticker", { it.security }),
        makeMetric("OrderId", { it.id }),
        makeMetric("OrderType", { it.orderType.name }),
        makeMetric("BuySell", { if (it.side == Side.Buy) "1" else "-1" }),
        makeMetric("EntryDate", { it.placementTime }),
        makeMetric("Price", { it.price }),
        makeMetric("Qty", { it.qtyLots })
)