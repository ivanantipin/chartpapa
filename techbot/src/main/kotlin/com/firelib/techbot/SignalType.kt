package com.firelib.techbot

import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.rsibolinger.RsiBolingerSignals
import com.firelib.techbot.sequenta.SequentaSignals
import com.firelib.techbot.sequenta.TdstLineSignals
import com.firelib.techbot.tdline.TdLineSignals

enum class SignalType(
    val signalGenerator: SignalGenerator,
    val msgEnum: MsgEnum
) {
    TREND_LINE(TdLineSignals, MsgEnum.TREND_LINE),
    DEMARK(SequentaSignals, MsgEnum.DEMARK),
    MACD(MacdSignals, MsgEnum.MACD),
    RSI_BOLINGER(RsiBolingerSignals, MsgEnum.RSI_BOLINGER),
    TDST(TdstLineSignals, MsgEnum.TDST),
}