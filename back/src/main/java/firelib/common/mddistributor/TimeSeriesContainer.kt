package firelib.common.mddistributor

import firelib.common.interval.Interval
import firelib.common.reader.MarketDataReader
import firelib.common.timeseries.TimeSeries
import firelib.common.timeseries.TimeSeriesImpl
import firelib.domain.Ohlc
import firelib.domain.merge
import java.time.Instant

class TimeSeriesContainer(val reader : MarketDataReader<Ohlc>) {

    private val timeSeries = ArrayList<TimeSeries<Ohlc>>()

    private val map = HashMap<Interval, TimeSeries<Ohlc>>()

    fun iterator(): List<Pair<Interval, TimeSeries<Ohlc>>> {
        return map.map { Pair(it.key, it.value) }
    }

    fun updatePrice(idx : Int, price : Double, vol : Long){
        timeSeries.forEach{it[0] = it[0].merge(price,vol)}
    }


    fun readTillIncluding(prevTime : Instant, time : Instant) : Boolean{
        while (true){

            val current = reader.current()

            if(current.endTime > prevTime && current.endTime <= time){
                timeSeries.forEach { it[0] = mergeOhlc(it[0], current) }
            }

            if(current.endTime >= time){
                break
            }

            if(!reader.read()){
                return false
            }
        }
        return true
    }

    operator fun set(interval: Interval, ts: TimeSeries<Ohlc>) {
        map[interval] = ts
        timeSeries += ts
    }

    fun contains(interval: Interval): Boolean {
        return map.contains(interval)
    }

    operator fun get(interval: Interval): TimeSeriesImpl<Ohlc> {
        return map[interval] as TimeSeriesImpl<Ohlc>
    }

    fun mergeOhlc(currOhlc: Ohlc, ohlc: Ohlc): Ohlc {
        require(!ohlc.interpolated, {"should not be interpolated"})

        if (currOhlc.interpolated) {
            require(!ohlc.endTime.isAfter(currOhlc.endTime), {"shall not be after ${currOhlc.endTime} < ${ohlc.endTime}"})
            return ohlc.copy(endTime = currOhlc.endTime, interpolated = false)
        } else {
            return currOhlc.copy(high = Math.max(ohlc.high, currOhlc.high),
                    low = Math.min(ohlc.low, currOhlc.low),
                    close = ohlc.close,
                    Oi = currOhlc.Oi + ohlc.Oi,
                    volume = currOhlc.volume + ohlc.volume
            )
        }
    }


}