package firelib.core.misc

import firelib.core.domain.Interval
import firelib.model.Model
import firelib.core.makePositionEqualsTo
import firelib.core.timeseries.nonInterpolatedView
import java.time.LocalTime


object PositionCloser {

    fun closePosByTimeoutAndTimeOfDay(model: Model,
                                      afterTime: LocalTime?,
                                      periods: Int,
                                      interval: Interval
    ) {

        model.orderManagers().forEachIndexed { idx, oms ->
            var cnt = 0

            val context = model.context

            context.mdDistributor.getOrCreateTs(idx, interval, 2).nonInterpolatedView().preRollSubscribe {cnt++}

            oms.tradesTopic().subscribe {
                cnt = 0
            }


            context.mdDistributor.addListener(interval) { inst, md ->
                if (oms.position() != 0
                        && !md.price(idx).interpolated
                        && cnt > periods
                        && (afterTime == null || afterTime < context.timeService.currentTimeUtc().toLocalTime())
                ) {
                    val price = md.price(idx)



                    oms.makePositionEqualsTo(0, price.close)
                }
            }

        }
    }

}