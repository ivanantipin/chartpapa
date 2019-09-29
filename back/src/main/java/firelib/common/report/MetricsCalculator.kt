package firelib.common.report

import firelib.common.Trade

typealias MetricsCalculator = ((List<Pair<Trade, Trade>>) -> Map<StrategyMetric, Double>)