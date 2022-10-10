package chart

import com.firelib.techbot.MsgLocalizer
import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.macd.RsiBolingerSignals
import com.firelib.techbot.sequenta.SequentaSignals
import com.firelib.techbot.sequenta.TdstLineSignals
import com.firelib.techbot.tdline.TdLineSignals

enum class SignalType(
    val settingsName: String,
    val signalGenerator: SignalGenerator,
    val msgLocalizer: MsgLocalizer
) {
    TREND_LINE("tl", TdLineSignals, MsgLocalizer.TREND_LINE),
    DEMARK("dema", SequentaSignals, MsgLocalizer.DEMARK),
    MACD("macd", MacdSignals, MsgLocalizer.MACD),
    RSI_BOLINGER("rbc", RsiBolingerSignals, MsgLocalizer.RSI_BOLINGER),
    TDST("tdst", TdstLineSignals, MsgLocalizer.TDST),
}

enum class BreachType {
    TREND_LINE, DEMARK_SIGNAL, TREND_LINE_SNAPSHOT, LEVELS_SNAPSHOT, LEVELS_SIGNAL, MACD, RSI_BOLINGER, TDST_SIGNAL,
}