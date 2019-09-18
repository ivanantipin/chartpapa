package firelib.common.report

import firelib.common.OrderStatus
import firelib.common.config.ModelBacktestConfig
import firelib.common.core.ModelOutput
import firelib.common.misc.jsonHelper
import org.apache.commons.io.FileUtils
import java.nio.file.Files
import java.nio.file.Paths
import java.nio.file.StandardCopyOption
import java.nio.file.StandardOpenOption

fun writeRows(ff: String, rows: Iterable<String>) {
    Files.write(Paths.get(ff), rows, StandardOpenOption.CREATE)
}


fun clearReportDir(targetDir: String) {

    try {
        FileUtils.deleteDirectory(Paths.get(targetDir).toFile())
    }catch (e : Exception){
        error("failed to remove report dir " + e.message)
    }

    FileUtils.forceMkdir(Paths.get(targetDir).toFile())
}


fun writeReport(model: ModelOutput, cfg: ModelBacktestConfig) {

    jsonHelper.serialize(cfg,Paths.get(cfg.reportTargetPath, "cfg.json"))

    if (model.trades.size == 0) {
        println("no trades generated")
        return
    }

    writeRows(Paths.get(cfg.reportTargetPath, "modelProps.properties").toAbsolutePath().toString(),model.modelProps.map({it.key + "=" + it.value}))

    val factors = model.trades[0].tradeStat.factors

    StreamTradeCaseWriter(cfg.getReportDbFile(), factors.map({it.key})).insertTrades(model.trades)

    StreamOrderWriter(cfg.getReportDbFile()).insertOrders(model.orderStates.filter {it.status == OrderStatus.New}.map {it.order})

    Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/StdReport.ipynb"),Paths.get(Paths.get(cfg.reportTargetPath,"StdReport.ipynb").toAbsolutePath().toString()),StandardCopyOption.REPLACE_EXISTING)
    Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/TradesReporter.py"),Paths.get(Paths.get(cfg.reportTargetPath,"TradesReporter.py").toAbsolutePath().toString()),StandardCopyOption.REPLACE_EXISTING)
    Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/ShowTrade.ipynb"),Paths.get(Paths.get(cfg.reportTargetPath,"ShowTrade.ipynb").toAbsolutePath().toString()),StandardCopyOption.REPLACE_EXISTING)

    println("report written to ${cfg.reportTargetPath} you can run it , command 'jupyter lab'")

}

