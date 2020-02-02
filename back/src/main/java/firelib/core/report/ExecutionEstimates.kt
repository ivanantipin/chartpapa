package firelib.core.report

data class ExecutionEstimates(val optParams: Map<String, Int>,
                         val metricToValue: Map<StrategyMetric, Double>)