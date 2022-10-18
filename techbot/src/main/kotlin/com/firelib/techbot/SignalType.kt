package com.firelib.techbot

import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.rsibolinger.RsiBolingerSignals
import com.firelib.techbot.sequenta.SequentaSignals
import com.firelib.techbot.sequenta.TdstLineSignals
import com.firelib.techbot.tdline.TdLineSignals

enum class SignalType(
    val signalGenerator: SignalGenerator,
    val msgLocalizer: MsgLocalizer
) {
    TREND_LINE(TdLineSignals, MsgLocalizer.TREND_LINE),
    DEMARK(SequentaSignals, MsgLocalizer.DEMARK),
    MACD(MacdSignals, MsgLocalizer.MACD),
    RSI_BOLINGER(RsiBolingerSignals, MsgLocalizer.RSI_BOLINGER),
    TDST(TdstLineSignals, MsgLocalizer.TDST),
}