package firelib.common.report


import firelib.common.core.ModelOutput
import firelib.common.misc.toTradingCases
import java.util.*

class ReportProcessor(val metricsCalculator : MetricsCalculator, val optimizedFunctionName: StrategyMetric,
                      val optParams: List<String>, val topModelsToKeep: Int = 3, val minNumberOfTrades: Int = 1, val removeOutlierTrades: Int = 2) {


    data class ModelStat (val optMetric : Double, val output : ModelOutput, val metrics : Map<StrategyMetric, Double>)

    private val bestModels_ = PriorityQueue<ModelStat>()//(Ordering.by((ms : ModelStat) -> ms.optMetric ).reverse)

    var estimates = ArrayList<ExecutionEstimates>()

    fun bestModels(): List<ModelOutput> { return bestModels_.map({it.output}).toList()}

    fun process(models: List<ModelOutput>) {

        val filtered: List<ModelOutput> = models.filter {it.trades.size >= minNumberOfTrades}

        println("model processed ${models.size} models met min trades count criteria ${filtered.size}")

        filtered.forEach({model ->

            val tradingCases = toTradingCases(model.trades)

            val metrics = metricsCalculator(tradingCases)

            val est = metrics[optimizedFunctionName]!!

            bestModels_ += ModelStat(est, model, metrics)
            if(bestModels_.size> topModelsToKeep)
                bestModels_.remove()

            estimates.add(ExecutionEstimates(extractOptParams(model.modelProps) , metrics))

        })
        println("total model complete ${estimates.size}")

    }

    private fun extractOptParams(props : Map<String,String>): Map<String, Int> {
        return optParams.associateBy({ it },{props[it]!!.toInt()})
    }
}