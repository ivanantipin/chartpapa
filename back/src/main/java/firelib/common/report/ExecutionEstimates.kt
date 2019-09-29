package firelib.common.report

data class ExecutionEstimates(val optParams: Map<String, Int>,
                         val metricToValue: Map<StrategyMetric, Double>)