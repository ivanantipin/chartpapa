package firelib.common.misc

import firelib.domain.Ohlc

class OhlcBuilderFromOhlc() {
    def appendOhlc(currOhlc : Ohlc, ohlc: Ohlc) {
        if (currOhlc.interpolated) {
            ohlcUtils.interpolate(ohlc,currOhlc)
            currOhlc.interpolated = false
        }else{
            currOhlc.H = math.max(ohlc.H, currOhlc.H)
            currOhlc.L = math.min(ohlc.L, currOhlc.L)
            currOhlc.C = ohlc.C
            currOhlc.Volume += ohlc.Volume
            currOhlc.Oi += ohlc.Oi
        }
    }
}
