package firelib.common.misc

import firelib.domain.Ohlc

class OhlcBuilderFromOhlc() {
    fun mergeOhlc(currOhlc: Ohlc, ohlc: Ohlc): Ohlc {
        if (currOhlc.interpolated) {
            return ohlc
        } else {
            return Ohlc(
                    high = Math.max(ohlc.high, currOhlc.high),
                    low = Math.min(ohlc.low, currOhlc.low),
                    close = ohlc.close,
                    Oi = currOhlc.Oi + ohlc.Oi,
                    volume = currOhlc.volume + ohlc.volume
            )
        }
    }
}