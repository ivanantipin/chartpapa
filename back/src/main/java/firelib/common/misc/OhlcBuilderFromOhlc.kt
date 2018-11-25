package firelib.common.misc

import firelib.domain.Ohlc

class OhlcBuilderFromOhlc() {
    fun mergeOhlc(currOhlc: Ohlc, ohlc: Ohlc): Ohlc {
        if (currOhlc.interpolated) {
            return ohlc
        } else {
            return Ohlc(
                    H = Math.max(ohlc.H, currOhlc.H),
                    L = Math.min(ohlc.L, currOhlc.L),
                    C = ohlc.C,
                    Volume = currOhlc.Volume + ohlc.Volume,
                    Oi = currOhlc.Oi + ohlc.Oi
            )
        }
    }
}