package firelib.core.report

import firelib.core.config.ModelBacktestConfig
import firelib.core.domain.ModelOutput
import firelib.core.misc.JsonHelper
import firelib.core.misc.toTradingCases
import firelib.core.report.dao.ColDefDao
import org.apache.commons.io.FileUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.statements.api.ExposedBlob
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.file.Paths
import kotlin.system.measureTimeMillis

object ReportWriter {

    val log = LoggerFactory.getLogger(javaClass)

    fun clearReportDir(targetDir: String) {
        try {
            FileUtils.deleteDirectory(Paths.get(targetDir).toFile())
        } catch (e: Exception) {
            error("failed to remove report dir " + e.message)
        }
        FileUtils.forceMkdir(Paths.get(targetDir).toFile())
    }

    fun writeReport(model: ModelOutput, cfg: ModelBacktestConfig) {
        if (model.trades.size == 0) {
            log.info("no trades generated")
        } else {

            val invalidTrades = model.trades.filter { it.tradeStat.factors.any { !it.second.isFinite() } }.count()
            if(invalidTrades > 0){
                log.error("number of invalid trades is ${invalidTrades} out of trades ${model.trades.size}")
            }
            measureTimeMillis {
                model.trades.groupBy { Pair(it.security(), it.order.modelName) }.values.forEach {
                    val cases = it.toTradingCases().filter { case->!case.first.tradeStat.factors.any {factor-> !factor.second.isFinite() } }
                    StreamTradeCaseWriter(cfg.getReportDbFile(), "trades").insertTrades(cases)
                }
            }.apply {
                println("took ${this / 1000.0} s to write report")
            }
        }
        ColDefDao(cfg.getReportDbFile(), orderColsDefs, "orders").upsert(model.orderStates)
        log.info("report written to ${cfg.reportTargetPath} you can run it , command 'jupyter lab'")
    }

    fun writeOpt(estimates: List<ExecutionEstimates>) {
        transaction {
            Opts.insert {
                it[Opts.blob] = ExposedBlob(JsonHelper.toJsonBytes(estimates))
            }
        }
    }
}

object Opts : Table("opts") {
    val blob = blob("blob")
}

