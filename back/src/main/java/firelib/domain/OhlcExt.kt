package firelib.domain

import firelib.common.misc.atUtc
import java.time.LocalDate

fun Ohlc.nprice(): Double = (close + high + low) / 3
fun Ohlc.medium(): Double = (high + low) / 2
fun Ohlc.isUpBar(): Boolean = close > open
fun Ohlc.range(): Double = high - low
fun Ohlc.upShadow(): Double = high - close
fun Ohlc.downShadow(): Double = close - low
fun Ohlc.bodyLength(): Double = Math.abs(close - open)
fun Ohlc.ret(): Double = (close - open) / open
fun Ohlc.isInRange(vv: Double): Boolean = high > vv && vv > low
fun Ohlc.merge(ohlc: Ohlc): Ohlc {
    return Ohlc(ohlc.endTime, open,
            Math.max(high, ohlc.high),
            Math.min(low, ohlc.low),
            ohlc.close, volume = ohlc.volume + volume
    )
}

fun Ohlc.merge(price: Double, vol: Long): Ohlc {
    if (interpolated) {
        return Ohlc(endTime, price, price, price, price, volume = vol, interpolated = false)
    }
    return Ohlc(endTime, open,
            high = Math.max(high, price),
            low = Math.min(low, price),
            close = price, volume = vol + volume
    )
}
fun Ohlc.date() : LocalDate {
    return endTime.atUtc().toLocalDate()!!
}
