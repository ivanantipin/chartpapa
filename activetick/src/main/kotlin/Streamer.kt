import at.feedapi.ActiveTickStreamListener
import at.shared.ATServerAPIDefines.ATQUOTESTREAM_TRADE_UPDATE
import java.lang.StringBuffer
import at.utils.jlib.PrintfFormat
import at.shared.ATServerAPIDefines.ATQUOTESTREAM_QUOTE_UPDATE
import at.shared.ATServerAPIDefines.ATMARKET_MOVERS_STREAM_UPDATE
import java.lang.StringBuilder

class Streamer(var m_session: APISession) : ActiveTickStreamListener(
    m_session.GetSession(), false
) {
    override fun OnATStreamTradeUpdate(update: ATQUOTESTREAM_TRADE_UPDATE) {
        var strSymbol = String(update.symbol.symbol)
        val plainSymbolIndex: Int = strSymbol.indexOf(0.toByte().toChar())
        strSymbol = strSymbol.substring(0, plainSymbolIndex)
        val sb = StringBuffer()
        sb.append("[")
        sb.append(update.lastDateTime.hour.toInt())
        sb.append(":")
        sb.append(update.lastDateTime.minute.toInt())
        sb.append(":")
        sb.append(update.lastDateTime.second.toInt())
        sb.append(":")
        sb.append(update.lastDateTime.milliseconds.toInt())
        sb.append("] STREAMTRADE [symbol:")
        sb.append(strSymbol)
        sb.append(" last:")
        val strFormat = "%0." + update.lastPrice.precision + "f"
        sb.append(PrintfFormat(strFormat).sprintf(update.lastPrice.price))
        sb.append(" lastSize:")
        sb.append(update.lastSize)
        sb.append("]")
        println(sb.toString())
    }

    override fun OnATStreamQuoteUpdate(update: ATQUOTESTREAM_QUOTE_UPDATE) {
        var strSymbol = String(update.symbol.symbol)
        val plainSymbolIndex: Int = strSymbol.indexOf(0.toByte().toChar())
        strSymbol = strSymbol.substring(0, plainSymbolIndex)
        val sb = StringBuffer()
        sb.append("[")
        sb.append(update.quoteDateTime.hour.toInt())
        sb.append(":")
        sb.append(update.quoteDateTime.minute.toInt())
        sb.append(":")
        sb.append(update.quoteDateTime.second.toInt())
        sb.append(":")
        sb.append(update.quoteDateTime.milliseconds.toInt())
        sb.append("] STREAMQUOTE [symbol:")
        sb.append(strSymbol)
        sb.append(" bid:")
        var strFormat = "%0." + update.bidPrice.precision + "f"
        sb.append(PrintfFormat(strFormat).sprintf(update.bidPrice.price))
        sb.append(" ask:")
        strFormat = "%0." + update.askPrice.precision + "f"
        sb.append(PrintfFormat(strFormat).sprintf(update.askPrice.price))
        sb.append(" bidSize:")
        sb.append(update.bidSize)
        sb.append(" askSize:")
        sb.append(update.askSize)
        sb.append("]")
        println(sb.toString())
    }

    override fun OnATStreamTopMarketMoversUpdate(update: ATMARKET_MOVERS_STREAM_UPDATE) {
        var strSymbol = String(update.marketMovers.symbol.symbol)
        val plainSymbolIndex: Int = strSymbol.indexOf(0.toByte().toChar())
        strSymbol = strSymbol.substring(0, plainSymbolIndex)
        val sb = StringBuffer()
        sb.append("RECV: [")
        sb.append(update.lastUpdateTime.hour.toInt())
        sb.append(":")
        sb.append(update.lastUpdateTime.minute.toInt())
        sb.append(":")
        sb.append(update.lastUpdateTime.second.toInt())
        sb.append(":")
        sb.append(update.lastUpdateTime.milliseconds.toInt())
        sb.append("] STREAMMOVERS [symbol:")
        sb.append(strSymbol)
        sb.append("]")
        println(sb.toString())
        var strFormat = ""
        for (i in update.marketMovers.items.indices) {
            val sb2 = StringBuilder()
            var strItemSymbol = String(update.marketMovers.items[i].symbol.symbol)
            val plainItemSymbolIndex: Int = strItemSymbol.indexOf(0.toByte().toChar())
            strItemSymbol = strItemSymbol.substring(0, plainItemSymbolIndex)
            sb2.append("symbol:")
            sb2.append(strItemSymbol)
            strFormat = "%0." + update.marketMovers.items[i].lastPrice.precision + "f"
            sb2.append("  \t[last:" + PrintfFormat(strFormat).sprintf(update.marketMovers.items[i].lastPrice.price))
            sb2.append(" volume:")
            sb2.append(update.marketMovers.items[i].volume)
            println(sb2.toString())
        }
        println("-------------------------------------------------------")
    }
}