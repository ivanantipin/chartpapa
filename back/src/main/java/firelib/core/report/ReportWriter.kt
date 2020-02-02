package firelib.core.report

import firelib.core.domain.OrderStatus
import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.ModelOutput
import firelib.core.misc.JsonHelper
import firelib.core.misc.toTradingCases
import firelib.core.report.dao.ColDefDao
import firelib.core.report.dao.StreamTradeCaseWriter
import org.apache.commons.io.FileUtils
import java.nio.file.*


object ReportWriter{
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

        writeStaticConf(cfg, model)

        copyPythonFiles(cfg)

        writeModelOutput(model, cfg)

        println("report written to ${cfg.reportTargetPath} you can run it , command 'jupyter lab'")

    }

    fun writeModelOutput(model: ModelOutput, cfg: ModelBacktestConfig){
        if (model.trades.size == 0) {
            println("no trades generated")
        }else{
            model.trades.groupBy { it.security() }.values.forEach {
                StreamTradeCaseWriter(cfg.getReportDbFile(), "trades").insertTrades(it.toTradingCases())
            }
        }

        ColDefDao(cfg.getReportDbFile(), orderColsDefs, "orders").upsert(model.orderStates.filter { it.status == OrderStatus.New }.map { it.order })
    }

    fun copyPythonFiles(cfg: ModelBacktestConfig) {
        //fixme
        Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/StdReport.ipynb"), Paths.get(Paths.get(cfg.reportTargetPath, "StdReport.ipynb").toAbsolutePath().toString()), StandardCopyOption.REPLACE_EXISTING)
        Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/TradesReporter.py"), Paths.get(Paths.get(cfg.reportTargetPath, "TradesReporter.py").toAbsolutePath().toString()), StandardCopyOption.REPLACE_EXISTING)
        Files.copy(Paths.get("/home/ivan/projects/chartpapa/market_research/report/ShowTrade.ipynb"), Paths.get(Paths.get(cfg.reportTargetPath, "ShowTrade.ipynb").toAbsolutePath().toString()), StandardCopyOption.REPLACE_EXISTING)
    }

    fun writeStaticConf(cfg: ModelBacktestConfig, model: ModelOutput) {

        JsonHelper.serialize(cfg, Paths.get(cfg.reportTargetPath, "cfg.json"))

        writeRows(Paths.get(cfg.reportTargetPath, "modelProps.properties").toAbsolutePath().toString(), model.modelProps.map({ it.key + "=" + it.value }))
    }

    fun writeOpt(path : Path, estimates: List<ExecutionEstimates>) {
        GenericMapWriter.write(path,estimates.map {
            it.metricToValue.mapKeys { it.key.name } + it.optParams.mapKeys { "opt_${it.key.replace('.','_')}" }
        }, "opts")
    }



}

