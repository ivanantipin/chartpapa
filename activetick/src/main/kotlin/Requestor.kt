import at.feedapi.ActiveTickServerRequester
import at.feedapi.Helpers
import at.shared.ATServerAPIDefines.ATQuoteDbResponseType
import at.shared.ATServerAPIDefines.QuoteDbResponseItem
import at.shared.ATServerAPIDefines.ATSymbolStatus
import at.shared.ATServerAPIDefines.QuoteDbDataItem
import at.shared.ATServerAPIDefines.ATDataType
import at.shared.ATServerAPIDefines.ATPRICE
import at.shared.ActiveTick.UInt64
import at.shared.ATServerAPIDefines.SYSTEMTIME
import java.lang.StringBuilder
import at.shared.ATServerAPIDefines.ATStreamResponseType
import at.shared.ATServerAPIDefines.ATQUOTESTREAM_DATA_ITEM
import at.shared.ATServerAPIDefines.ATBarHistoryResponseType
import at.shared.ATServerAPIDefines.ATBARHISTORY_RECORD
import at.utils.jlib.PrintfFormat
import at.shared.ATServerAPIDefines.ATTickHistoryResponseType
import at.shared.ATServerAPIDefines.ATTICKHISTORY_RECORD
import at.shared.ATServerAPIDefines.ATTickHistoryRecordType
import at.shared.ATServerAPIDefines.ATTICKHISTORY_TRADE_RECORD
import at.shared.ATServerAPIDefines.ATTICKHISTORY_QUOTE_RECORD
import at.shared.ATServerAPIDefines.ATMARKET_HOLIDAYSLIST_ITEM
import at.shared.ATServerAPIDefines.ATMarketMoversDbResponseType
import at.shared.ATServerAPIDefines.ATMARKET_MOVERS_RECORD
import at.shared.ATServerAPIDefines.ATMARKET_MOVERS_STREAM_RESPONSE
import at.shared.ATServerAPIDefines.ATSYMBOL
import at.shared.ActiveTick.DateTime
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.*

class Requestor(apiSession: APISession, streamer: Streamer?) :
    ActiveTickServerRequester(apiSession.GetServerAPI(), apiSession.GetSession(), streamer) {
    override fun OnQuoteDbResponse(
        origRequest: Long,
        responseType: ATQuoteDbResponseType,
        vecData: Vector<QuoteDbResponseItem>
    ) {
        val strResponseType = when (responseType.m_atQuoteDbResponseType) {
            ATQuoteDbResponseType.QuoteDbResponseSuccess -> "QuoteDbResponseSuccess"
            ATQuoteDbResponseType.QuoteDbResponseInvalidRequest -> "QuoteDbResponseInvalidRequest"
            ATQuoteDbResponseType.QuoteDbResponseDenied ->  "QuoteDbResponseDenied"
            else -> ""
        }
        println("RECV ($origRequest): QuoteDb response $strResponseType\n-------------------------------------------------------------------------")

        vecData.iterator().forEach { responseItem->
            val strSymbolStatus = decodeSymbolStatus(responseItem)
            val strItemSymbol = String(responseItem.m_atResponse.symbol.symbol).substringBefore(0.toChar())
            println("\tsymbol: [$strItemSymbol] symbolStatus: $strSymbolStatus")
            val itrDataItems: Iterator<QuoteDbDataItem> = responseItem.m_vecDataItems.iterator()
            while (responseItem.m_atResponse.status.m_atSymbolStatus == ATSymbolStatus.SymbolStatusSuccess && itrDataItems.hasNext()) {
                val dataItem = itrDataItems.next()
                val sb = StringBuilder()
                sb.append(
                    """	ATQuoteFieldType:${dataItem.m_dataItem.fieldType.m_atQuoteFieldType}
"""
                )
                sb.append(
                    """	ATFieldStatus:${dataItem.m_dataItem.fieldStatus.m_atFieldStatus}
"""
                )
                sb.append(
                    """	ATDataType:${dataItem.m_dataItem.dataType.m_atDataType}
"""
                )
                sb.append("\tData:${dodo(dataItem)}")
                println(sb.toString())
            }
            println("\t-------------------------------------")
        }

    }

    private fun decodeSymbolStatus(responseItem: QuoteDbResponseItem): String {
        val strSymbolStatus = when (responseItem.m_atResponse.status.m_atSymbolStatus) {
            ATSymbolStatus.SymbolStatusSuccess -> "SymbolStatusSuccess"
            ATSymbolStatus.SymbolStatusInvalid -> "SymbolStatusInvalid"
            ATSymbolStatus.SymbolStatusUnavailable -> "SymbolStatusUnavailable"
            ATSymbolStatus.SymbolStatusNoPermission -> "SymbolStatusNoPermission"
            else -> ""
        }
        return strSymbolStatus
    }

    private fun dodo(dataItem: QuoteDbDataItem): String {
        val intBytes = ByteArray(4)
        val longBytes = ByteArray(8)
        return when (dataItem.m_dataItem.dataType.m_atDataType) {
            ATDataType.Byte -> String(dataItem.GetItemData())
            ATDataType.ByteArray -> "byte data"
            ATDataType.UInteger32 -> {
                System.arraycopy(dataItem.GetItemData(), 0, intBytes, 0, 4)
                ByteBuffer.wrap(intBytes).order(ByteOrder.LITTLE_ENDIAN).int.toString()
            }
            ATDataType.UInteger64 -> {
                System.arraycopy(dataItem.GetItemData(), 0, longBytes, 0, 8)
                ByteBuffer.wrap(longBytes).order(ByteOrder.LITTLE_ENDIAN).long.toString()
            }
            ATDataType.Integer32 -> {
                System.arraycopy(dataItem.GetItemData(), 0, intBytes, 0, 4)
                ByteBuffer.wrap(intBytes).order(ByteOrder.LITTLE_ENDIAN).int.toString()
            }
            ATDataType.Integer64 -> {
                System.arraycopy(dataItem.GetItemData(), 0, longBytes, 0, 8)
                ByteBuffer.wrap(longBytes).order(ByteOrder.LITTLE_ENDIAN).long.toString()
            }
            ATDataType.Price -> {
                Helpers.BytesToPrice(dataItem.GetItemData()).toString()
            }
            ATDataType.String -> {
                String(dataItem.GetItemData())
            }
            ATDataType.UnicodeString -> {
                String(dataItem.GetItemData())
            }
            ATDataType.DateTime -> {
                val li = UInt64(dataItem.GetItemData())
                val dateTime = DateTime.GetDateTime(li)
                val sb = StringBuilder()
                sb.append(dateTime.month.toInt())
                sb.append("/")
                sb.append(dateTime.day.toInt())
                sb.append("/")
                sb.append(dateTime.year.toInt())
                sb.append(" ")
                sb.append(dateTime.hour.toInt())
                sb.append(":")
                sb.append(dateTime.minute.toInt())
                sb.append(":")
                sb.append(dateTime.second.toInt())
                sb.toString()
            }
            else -> {
                ""
            }
        }
    }

    override fun OnRequestTimeoutCallback(origRequest: Long) {
        println("($origRequest): Request timed-out")
    }

    override fun OnQuoteStreamResponse(
        origRequest: Long,
        responseType: ATStreamResponseType,
        vecData: Vector<ATQUOTESTREAM_DATA_ITEM>
    ) {
        var strResponseType = ""
        when (responseType.m_responseType) {
            ATStreamResponseType.StreamResponseSuccess -> strResponseType = "StreamResponseSuccess"
            ATStreamResponseType.StreamResponseInvalidRequest -> strResponseType = "StreamResponseInvalidRequest"
            ATStreamResponseType.StreamResponseDenied -> strResponseType = "StreamResponseDenied"
            else -> {
            }
        }
        println("RECV ($origRequest): Quote stream response [$strResponseType]\n--------------------------------------------------------------")
        if (responseType.m_responseType == ATStreamResponseType.StreamResponseSuccess) {
            var strSymbolStatus = ""
            val itrDataItems: Iterator<ATQUOTESTREAM_DATA_ITEM> = vecData.iterator()
            while (itrDataItems.hasNext()) {
                when (itrDataItems.next().symbolStatus.m_atSymbolStatus) {
                    ATSymbolStatus.SymbolStatusSuccess -> strSymbolStatus = "SymbolStatusSuccess"
                    ATSymbolStatus.SymbolStatusInvalid -> strSymbolStatus = "SymbolStatusInvalid"
                    ATSymbolStatus.SymbolStatusUnavailable -> strSymbolStatus = "SymbolStatusUnavailable"
                    ATSymbolStatus.SymbolStatusNoPermission -> strSymbolStatus = "SymbolStatusNoPermission"
                    else -> {
                    }
                }
                println("\tsymbol:$strSymbolStatus symbolStatus: $strSymbolStatus")
            }
        }
    }

    override fun OnBarHistoryDbResponse(
        origRequest: Long,
        responseType: ATBarHistoryResponseType,
        vecData: Vector<ATBARHISTORY_RECORD>
    ) {
        var strResponseType = ""
        when (responseType.m_responseType) {
            ATBarHistoryResponseType.BarHistoryResponseSuccess.toInt() -> strResponseType = "BarHistoryResponseSuccess"
            ATBarHistoryResponseType.BarHistoryResponseInvalidRequest.toInt() -> strResponseType =
                "BarHistoryResponseInvalidRequest"
            ATBarHistoryResponseType.BarHistoryResponseMaxLimitReached.toInt() -> strResponseType =
                "BarHistoryResponseMaxLimitReached"
            ATBarHistoryResponseType.BarHistoryResponseDenied.toInt() -> strResponseType = "BarHistoryResponseDenied"
            else -> {
            }
        }
        println("RECV ($origRequest): Bar History response [$strResponseType]\n--------------------------------------------------------------")
        val itrDataItems: Iterator<ATBARHISTORY_RECORD> = vecData.iterator()
        var index = 0
        val recCount = vecData.size
        var strFormat = "%0.2f"
        while (itrDataItems.hasNext()) {
            val record = itrDataItems.next()
            val sb = StringBuilder()
            sb.append((++index).toString() + "/" + recCount + " ")
            sb.append("[" + record.barTime.month + "/" + record.barTime.day + "/" + record.barTime.year + " ")
            sb.append(record.barTime.hour.toString() + ":" + record.barTime.minute + ":" + record.barTime.second + "] ")
            strFormat = "%0." + record.open.precision + "f"
            sb.append("  \t[o:" + PrintfFormat(strFormat).sprintf(record.open.price))
            strFormat = "%0." + record.high.precision + "f"
            sb.append("  \th:" + PrintfFormat(strFormat).sprintf(record.high.price) + " ")
            strFormat = "%0." + record.low.precision + "f"
            sb.append("  \tl:" + PrintfFormat(strFormat).sprintf(record.low.price) + " ")
            strFormat = "%0." + record.close.precision + "f"
            sb.append("  \tc:" + PrintfFormat(strFormat).sprintf(record.close.price) + " ")
            sb.append("  \tvol:" + record.volume)
            println(sb.toString())
        }
        println("--------------------------------------------------------------\nTotal records:$recCount")
    }

    override fun OnTickHistoryDbResponse(
        origRequest: Long,
        responseType: ATTickHistoryResponseType,
        vecData: Vector<ATTICKHISTORY_RECORD>
    ) {
        var strResponseType = ""
        when (responseType.m_responseType) {
            ATTickHistoryResponseType.TickHistoryResponseSuccess -> strResponseType = "TickHistoryResponseSuccess"
            ATTickHistoryResponseType.TickHistoryResponseInvalidRequest -> strResponseType =
                "TickHistoryResponseInvalidRequest"
            ATTickHistoryResponseType.TickHistoryResponseMaxLimitReached -> strResponseType =
                "TickHistoryResponseMaxLimitReached"
            ATTickHistoryResponseType.TickHistoryResponseDenied -> strResponseType = "TickHistoryResponseDenied"
            else -> {
            }
        }
        println("RECV ($origRequest): Tick history response [$strResponseType]\n--------------------------------------------------------------")
        val itrDataItems: Iterator<ATTICKHISTORY_RECORD> = vecData.iterator()
        var index = 0
        val recCount = vecData.size
        var strFormat = "%0.2f"
        while (itrDataItems.hasNext()) {
            val record = itrDataItems.next()
            when (record.recordType.m_historyRecordType) {
                ATTickHistoryRecordType.TickHistoryRecordTrade -> {
                    val atTradeRecord = record as ATTICKHISTORY_TRADE_RECORD
                    val sb = StringBuilder()
                    sb.append("[")
                    sb.append(++index)
                    sb.append("/")
                    sb.append(recCount)
                    sb.append("]")
                    sb.append(" [" + atTradeRecord.lastDateTime.month + "/" + atTradeRecord.lastDateTime.day + "/" + atTradeRecord.lastDateTime.year + " ")
                    sb.append(atTradeRecord.lastDateTime.hour.toString() + ":" + atTradeRecord.lastDateTime.minute + ":" + atTradeRecord.lastDateTime.second + "] ")
                    sb.append("TRADE ")
                    strFormat = "%0." + atTradeRecord.lastPrice.precision + "f"
                    sb.append("  \t[last:" + PrintfFormat(strFormat).sprintf(atTradeRecord.lastPrice.price))
                    sb.append("  \tlastsize:" + atTradeRecord.lastSize)
                    sb.append("  \tlastexch:" + atTradeRecord.lastExchange.m_atExchangeType)
                    sb.append("  \tcond:" + atTradeRecord.lastCondition[0].m_atTradeConditionType)
                    println(sb.toString())
                }
                ATTickHistoryRecordType.TickHistoryRecordQuote -> {
                    val atQuoteRecord = record as ATTICKHISTORY_QUOTE_RECORD
                    val sb = StringBuilder()
                    sb.append("[")
                    sb.append(++index)
                    sb.append("/")
                    sb.append(recCount)
                    sb.append("]")
                    sb.append(" [" + atQuoteRecord.quoteDateTime.month + "/" + atQuoteRecord.quoteDateTime.day + "/" + atQuoteRecord.quoteDateTime.year + " ")
                    sb.append(atQuoteRecord.quoteDateTime.hour.toString() + ":" + atQuoteRecord.quoteDateTime.minute + ":" + atQuoteRecord.quoteDateTime.second + "] ")
                    sb.append("QUOTE ")
                    strFormat = "%0." + atQuoteRecord.bidPrice.precision + "f"
                    sb.append("  \t[bid:" + PrintfFormat(strFormat).sprintf(atQuoteRecord.bidPrice.price))
                    strFormat = "%0." + atQuoteRecord.askPrice.precision + "f"
                    sb.append("  \task:" + PrintfFormat(strFormat).sprintf(atQuoteRecord.askPrice.price) + " ")
                    sb.append("  \tbidsize:" + atQuoteRecord.bidSize)
                    sb.append("  \tasksize:" + atQuoteRecord.askSize)
                    sb.append("  \tbidexch:" + atQuoteRecord.bidExchange.m_atExchangeType)
                    sb.append("  \taskexch:" + atQuoteRecord.askExchange.m_atExchangeType)
                    sb.append("  \tcond:" + atQuoteRecord.quoteCondition.m_quoteConditionType)
                    println(sb.toString())
                }
            }
        }
        println("--------------------------------------------------------------\nTotal records:$recCount")
    }

    override fun OnMarketHolidaysResponse(origRequest: Long, vecData: Vector<ATMARKET_HOLIDAYSLIST_ITEM>) {
        println("RECV ($origRequest): MarketHolidays response \n--------------------------------------------------------------")
        val itrDataItems: Iterator<ATMARKET_HOLIDAYSLIST_ITEM> = vecData.iterator()
        var index = 0
        val recCount = vecData.size
        while (itrDataItems.hasNext()) {
            val item = itrDataItems.next()
            val sb = StringBuilder()
            sb.append("[")
            sb.append(++index)
            sb.append("/")
            sb.append(recCount)
            sb.append("]")
            sb.append(" [" + item.beginDateTime.month + "/" + item.beginDateTime.day + "/" + item.beginDateTime.year + " ")
            sb.append(item.beginDateTime.hour.toString() + ":" + item.beginDateTime.minute + ":" + item.beginDateTime.second + "] ")
            sb.append(" - ")
            sb.append(" [" + item.endDateTime.month + "/" + item.endDateTime.day + "/" + item.endDateTime.year + " ")
            sb.append(item.endDateTime.hour.toString() + ":" + item.endDateTime.minute + ":" + item.endDateTime.second + "] ")
            if (item.endDateTime.day == item.beginDateTime.day &&
                item.endDateTime.hour - item.beginDateTime.hour >= 8
            ) {
                sb.append(" -- ALL DAY ")
            }
            println(sb.toString())
        }
        println("--------------------------------------------------------------\nTotal records:$recCount")
    }

    override fun OnMarketMoversDbResponse(
        origRequest: Long,
        responseType: ATMarketMoversDbResponseType,
        vecData: Vector<ATMARKET_MOVERS_RECORD>
    ) {
        var strResponseType = ""
        when (responseType.m_responseType) {
            ATMarketMoversDbResponseType.MarketMoversDbResponseSuccess -> strResponseType =
                "MarketMoversDbResponseSuccess"
            ATMarketMoversDbResponseType.MarketMoversDbResponseInvalidRequest -> strResponseType =
                "MarketMoversDbResponseInvalidRequest"
            ATMarketMoversDbResponseType.MarketMoversDbResponseDenied -> strResponseType =
                "MarketMoversDbResponseDenied"
            else -> {
            }
        }
        println("RECV ($origRequest): Market Movers response [ $strResponseType]\n--------------------------------------------------------------")
        val itrMarketMovers: Iterator<ATMARKET_MOVERS_RECORD> = vecData.iterator()
        var strFormat = ""
        while (itrMarketMovers.hasNext()) {
            val record = itrMarketMovers.next()
            var strSymbol = String(record.symbol.symbol)
            val plainSymbolIndex: Int = strSymbol.indexOf(0.toChar())
            if (plainSymbolIndex > 0) strSymbol = strSymbol.substring(0, plainSymbolIndex)
            println("Market movers symbol: $strSymbol\n------------------\n")
            for (i in record.items.indices) {
                var strItemSymbol = String(record.items[i].symbol.symbol)
                val plainItemSymbolIndex: Int = strItemSymbol.indexOf(0.toByte().toChar())
                strItemSymbol = if (plainItemSymbolIndex > 0) strItemSymbol.substring(0, plainItemSymbolIndex) else ""
                val sb = StringBuilder()
                sb.append("symbol:")
                sb.append(strItemSymbol)
                strFormat = "%0." + record.items[i].lastPrice.precision + "f"
                sb.append("  \tlast:" + PrintfFormat(strFormat).sprintf(record.items[i].lastPrice.price))
                sb.append(" volume:")
                sb.append(record.items[i].volume)
                var strName = String(record.items[i].name)
                val plainNameIndex: Int = strName.indexOf(0.toChar())
                strName = if (plainNameIndex > 0) strName.substring(0, plainNameIndex - 1) else ""
                sb.append(" name: $strName")
                println(sb.toString())
            }
        }
    }

    override fun OnMarketMoversStreamResponse(
        origRequest: Long,
        responseType: ATStreamResponseType,
        response: ATMARKET_MOVERS_STREAM_RESPONSE
    ) {
        var strResponseType = ""
        when (responseType.m_responseType) {
            ATStreamResponseType.StreamResponseSuccess -> strResponseType = "StreamResponseSuccess"
            ATStreamResponseType.StreamResponseInvalidRequest -> strResponseType = "StreamResponseInvalidRequest"
            ATStreamResponseType.StreamResponseDenied -> strResponseType = "StreamResponseDenied"
            else -> {
            }
        }
        println("RECV ($origRequest): Market Movers response [ $strResponseType]\n--------------------------------------------------------------")
    }

    override fun OnConstituentListResponse(origRequest: Long, vecData: Vector<ATSYMBOL>) {
        var strResponseType = ""
        for (i in vecData.indices) {
            val symbol = Helpers.SymbolToString(vecData.elementAt(i))
            strResponseType += """
                $symbol
                
                """.trimIndent()
        }
        println("RECV ($origRequest): Constituent list response [ $strResponseType]\n--------------------------------------------------------------")
    }
}