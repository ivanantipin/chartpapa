package firelib.common.report

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelOutput
import firelib.common.misc.jsonHelper
import firelib.common.misc.writeRows
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption


fun clearReportDir(targetDir: String) : Unit {

    try {
        FileUtils.deleteDirectory(Paths.get(targetDir).toFile())
    }catch (e : Exception){
        error("failed to remove report dir " + e.message)
    }

    FileUtils.forceMkdir(Paths.get(targetDir).toFile())
}


fun writeReport(model: ModelOutput, cfg: ModelBacktestConfig, targetDir: String) : Unit {

    jsonHelper.serialize(cfg,Paths.get(targetDir, "cfg.json"))

    var trades = model.trades

    if (trades.size == 0) return

    writeRows(Paths.get(targetDir, "modelProps.properties").toAbsolutePath().toString(),model.modelProps.map({it.key + "=" + it.value}))

    val factors = trades[0].tradeStat.factors

    val tradeWriter = StreamTradeCaseWriter(Paths.get(targetDir, "trades.csv").toAbsolutePath(), factors.map({it.key}))
    tradeWriter.writeHeader()
    model.trades.forEach(tradeWriter)

    val orderWriter = StreamOrderWriter(Paths.get(targetDir, "orders.csv").toAbsolutePath())
    orderWriter.writeHeader()
    model.orderStates.filter {it.status == OrderStatus.New}.map {it.order}.forEach(orderWriter)

    Files.copy(Paths.get("/home/ivan/projects/fbackend/market_research/report/StdReport.ipynb"),Paths.get(Paths.get(targetDir,"StdReport.ipynb").toAbsolutePath().toString()),StandardCopyOption.REPLACE_EXISTING)
    Files.copy(Paths.get("/home/ivan/projects/fbackend/market_research/report/TradesReporter.py"),Paths.get(Paths.get(targetDir,"TradesReporter.py").toAbsolutePath().toString()),StandardCopyOption.REPLACE_EXISTING)

    System.out.println("report written to $targetDir you can run it , command 'ipython notebook StdReport.ipynb'")

}

