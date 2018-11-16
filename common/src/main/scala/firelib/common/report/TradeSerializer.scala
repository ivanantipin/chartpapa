package firelib.common.report

import firelib.common.misc.{DateUtils, utils}
import firelib.common.{Side, Trade}

trait TradeSerializer extends ReportConsts with DateUtils{

    val colsDef = List[(String,((Trade,Trade))=>String)](
        ("Ticker",t=>t._1.security),
        ("OrderId0",t=>t._1.order.id),
        ("OrderId1",t=>t._2.order.id),
        ("BuySell",t=>if(t._1.side == Side.Buy) "1" else "-1"),
        ("EntryDate",t=>t._1.dtGmt.toStandardString),
        ("EntryPrice",t=>utils.dbl2Str(t._1.price, decPlaces)),
        ("ExitDate",t=>t._2.dtGmt.toStandardString),
        ("ExitPrice",t=>utils.dbl2Str(t._2.price, decPlaces)),
        ("Pnl",t=>utils.dbl2Str(utils.pnlForCase(t), decPlaces)),
        ("Qty",t=>t._1.qty.toString),
        ("MAE",t=>utils.dbl2Str(t._2.tradeStat.MAE, decPlaces)),
        ("MFE",t=>utils.dbl2Str(t._2.tradeStat.MFE, decPlaces))
    )

    def getHeader(): Seq[String] = (colsDef.map(_._1) )

    def serialize(t : (Trade,Trade)) : Seq[String] = {
        colsDef.map(_._2).map(_(t))
    }
}
