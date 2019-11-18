package firelib.common.misc

import firelib.common.interval.Interval
import firelib.common.model.Model
import firelib.common.ordermanager.makePositionEqualsTo
import firelib.common.timeseries.nonInterpolatedView
import java.time.LocalTime


object PositionCloser {

    fun closePosByTimeoutAndTimeOfDay(model: Model,
                                      afterTime: LocalTime?,
                                      periods: Int,
                                      interval: Interval
    ) {

        model.orderManagers().forEachIndexed({ idx, oms ->
            var cnt = 0

            val context = model.context

            context.mdDistributor.getOrCreateTs(idx, interval, 2).nonInterpolatedView().preRollSubscribe {cnt++}

            oms.tradesTopic().subscribe({
                cnt = 0
            })


            context.mdDistributor.addListener(Interval.Min10, { inst, md ->
                if (oms.position() != 0
                        && !md.price(idx).interpolated
                        && cnt > periods
                        && (afterTime == null || afterTime < context.timeService.currentTimeUtc().toLocalTime())
                ) {
                    oms.makePositionEqualsTo(0)
                }
            })

        })
    }

}