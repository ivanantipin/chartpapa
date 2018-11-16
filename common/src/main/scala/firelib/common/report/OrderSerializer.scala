package firelib.common.report

import firelib.common.misc.{DateUtils, utils}
import firelib.common.{Order, Side}

trait OrderSerializer extends ReportConsts with DateUtils{
    val orderColsDef = List[(String,Order=>String)](
        ("Ticker",o=>o.security),
        ("OrderId",o=>o.id),
        ("OrderType",o=>o.orderType.Name),
        ("BuySell",o=>if(o.side == Side.Buy) "1" else "-1"),
        ("EntryDate",o=>o.placementTime.toStandardString),
        ("Price",o=>utils.dbl2Str(o.price, decPlaces)),
        ("Qty",o=>o.qty.toString)
    )

    def serializeOrder(order : Order) : List[String] = orderColsDef.map(_._2).map(_(order))

    def serialize (order : Order ) : String = serializeOrder(order).mkString(separator)
    def getHeader(): String = (orderColsDef.map(_._1) ).mkString(separator)
}



