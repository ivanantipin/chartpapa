package firelib.core.report

import firelib.core.domain.OrderState
import firelib.core.domain.Side
import firelib.core.report.dao.ColDef

val orderColsDefs: Array<ColDef<OrderState, out Any>> = arrayOf(
        makeMetric("Ticker", { it.order.security }),
        makeMetric("OrderId", { it.order.id }),
        makeMetric("OrderType", { it.order.orderType.name }),
        makeMetric("BuySell", { if (it.order.side == Side.Buy) "1" else "-1" }),
        makeMetric("EntryDate", { it.order.placementTime }),
        makeMetric("Price", { it.order.price }),
        makeMetric("Qty", { it.order.qtyLots }),
        makeMetric("Status", { it.status.name }),
        makeMetric("Msg", { (it.msg ?: "") as String })
)