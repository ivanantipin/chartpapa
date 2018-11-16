package firelib.common.timeboundscalc

import java.time.Instant

import firelib.common.config.ModelBacktestConfig

/**

 */
trait TimeBoundsCalculator extends (ModelBacktestConfig=>(Instant,Instant))
