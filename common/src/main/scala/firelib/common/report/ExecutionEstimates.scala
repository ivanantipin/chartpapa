package firelib.common.report

import scala.collection.Map

class ExecutionEstimates(val optParams: Map[String, Int],
                         val metricToValue: Map[StrategyMetric, Double])
