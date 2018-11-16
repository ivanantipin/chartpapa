package firelib.common.report

import java.io.{BufferedOutputStream, OutputStreamWriter}
import java.nio.file.StandardOpenOption._
import java.nio.file.{Files, Path, Paths}

import firelib.common.OrderStatus
import firelib.common.core.{BacktestMode, BindModelComponent, ModelConfigContext}
import firelib.common.interval.Interval
import firelib.common.mddistributor.MarketDataDistributorComponent
import firelib.common.misc.{DateUtils, WindowSlicer, utils}
import firelib.common.model.Model
import firelib.domain.Ohlc


trait OhlcReportWriterComponent {
    this : ModelConfigContext with MarketDataDistributorComponent with BindModelComponent =>

    var mdWriter : OhlcReportWriter =_

    if(modelConfig.dumpOhlcData && modelConfig.backtestMode == BacktestMode.SimpleRun){
        onModelBinded.subscribe(m=>{
            assert(mdWriter == null)
            mdWriter = new OhlcReportWriter(m)
        })
    }

    class OhlcReportWriter(val model : Model, val minsWindow : Int = 100) extends DateUtils{
        for(instrIdx <- 0 until modelConfig.instruments.length){
            val slicer = new WindowSlicer[Ohlc](minsWindow.minute)
            model.orderManagers(instrIdx).tradesTopic.subscribe(t=>slicer.updateWriteBefore())
            model.orderManagers(instrIdx).orderStateTopic.filter(_.status == OrderStatus.New).subscribe(o=>slicer.updateWriteBefore())
            val ts = marketDataDistributor.activateOhlcTimeSeries(instrIdx,Interval.Min1,minsWindow)
            val ohlcPath = Paths.get(modelConfig.reportTargetPath).resolve(s"ohlc_${modelConfig.instruments(instrIdx).ticker}.csv")
            ts.onNewBar.map(_(0)).lift(slicer).subscribe(new OhlcStreamWriter(ohlcPath))
        }
    }
}


class OhlcStreamWriter(val path : Path) extends (Ohlc=>Unit) with ReportConsts with DateUtils{

    val colsDef = List[(String,Ohlc=>String)](
        ("DT",o=>o.dtGmtEnd.toStandardString),
        ("O",o=>utils.dbl2Str(o.O,5)),
        ("H",o=>utils.dbl2Str(o.H,5)),
        ("L",o=>utils.dbl2Str(o.L,5)),
        ("C",o=>utils.dbl2Str(o.C,5))
    )


    val stream =  new OutputStreamWriter(new BufferedOutputStream(Files.newOutputStream(path, CREATE, APPEND)))

    stream.write(colsDef.map(_._1).mkString(separator) + '\n')



    override def apply(ohlc: Ohlc): Unit = {
        stream.write(colsDef.map(_._2).map(_(ohlc)).mkString(";") :+ '\n')
        stream.flush()
    }
}
