package chart

import com.firelib.techbot.SignalGenerator
import com.firelib.techbot.macd.MacdSignals
import com.firelib.techbot.macd.RsiBolingerSignals
import com.firelib.techbot.sequenta.SequentaSignals
import com.firelib.techbot.sequenta.TdstLineSignals
import com.firelib.techbot.tdline.TdLineSignals

enum class SignalType(
    val settingsName : String,
    val signalGenerator: SignalGenerator
){
    TREND_LINE("tl", TdLineSignals),
    DEMARK("dema",  SequentaSignals),
    MACD("macd", MacdSignals),
    RSI_BOLINGER("rbc", RsiBolingerSignals),
    TDST("tdst", TdstLineSignals),
}

enum class BreachType {
    TREND_LINE, DEMARK_SIGNAL, TREND_LINE_SNAPSHOT, LEVELS_SNAPSHOT, LEVELS_SIGNAL, MACD, RSI_BOLINGER, TDST_SIGNAL,
}