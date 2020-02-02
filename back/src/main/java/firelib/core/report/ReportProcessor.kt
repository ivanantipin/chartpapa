package firelib.core.report


import firelib.core.ModelOutput
import firelib.common.misc.toTradingCases
import firelib.core.report.StatCalculator.statCalculator
import java.util.*

class ReportProcessor(val optimizedFunctionName: StrategyMetric,
                      val optParams: List<String>, val topModelsToKeep: Int = 3, val minNumberOfTrades: Int = 20, val removeOutlierTrades: Int = 2) {


    data class ModelStat (val optMetric : Double, val output : ModelOutput, val metrics : Map<StrategyMetric, Double>)

    private val bestModels_ = PriorityQueue<ModelStat>({ p0, p1 -> p0.optMetric.compareTo(p1.optMetric) })//(Ordering.by((ms : ModelStat) -> ms.optMetric ).reverse)

    var estimates = mutableListOf<ExecutionEstimates>()

    fun bestModels(): List<ModelOutput> { return bestModels_.map{it.output}}

    fun process(models: List<ModelOutput>) {

        val filtered: List<ModelOutput> = models.filter {it.trades.size >= minNumberOfTrades}

        println("model processed ${models.size} models met min trades count criteria ${filtered.size}")

        filtered.forEach { output ->

            val tradingCases = output.trades.toTradingCases()

            val metrics = statCalculator(tradingCases)

            val est = metrics[optimizedFunctionName]!!

            bestModels_ += ModelStat(est, output, metrics)
            if(bestModels_.size> topModelsToKeep)
                bestModels_.poll()

            estimates.add(ExecutionEstimates(extractOptParams(output.modelProps) , metrics))

        }
        println("total model complete ${estimates.size}")

    }

    private fun extractOptParams(props : Map<String,String>): Map<String, Int> {
        return optParams.associateBy({ it },{ (props[it] ?: error("")).toInt()})
    }
}