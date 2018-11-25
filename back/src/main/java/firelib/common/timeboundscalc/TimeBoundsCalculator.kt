package firelib.common.timeboundscalc

import java.time.Instant

import firelib.common.config.ModelBacktestConfig

/**

 */
interface TimeBoundsCalculator : ((ModelBacktestConfig)->Pair<Instant,Instant>)