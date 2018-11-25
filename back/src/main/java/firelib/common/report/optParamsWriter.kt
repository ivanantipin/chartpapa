package firelib.common.report

import firelib.common.misc.dbl2Str
import firelib.common.misc.writeRows
import firelib.common.opt.OptimizedParameter
import firelib.common.report.ReportConsts.Companion.decPlaces
import firelib.common.report.ReportConsts.Companion.separator
import java.nio.file.Paths
import java.time.Instant

object optParamsWriter : ReportConsts{

    fun write(targetDir: String, optEnd: Instant, estimates: List<ExecutionEstimates>, optParams: List<OptimizedParameter>, metrics: List<StrategyMetric>) {
        var rows = ArrayList<String>()

        rows.add(optParams.map{it.name}.union(metrics.map{it.name}).joinToString(separator))
        for (est in estimates) {
            val opts: List<String> = optParams.map({est.optParams[it.name].toString()})
            val calcMetrics: List<String> = metrics.map({
                dbl2Str(est.metricToValue[it]!!, decPlaces)
            })
            rows.add(opts.union(calcMetrics).joinToString(separator))
        }
        writeRows(Paths.get(targetDir, "Opt.csv").toString(), rows)
    }
}