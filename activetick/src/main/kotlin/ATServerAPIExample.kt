import at.shared.ATServerAPIDefines
import at.shared.ATServerAPIDefines.ATSYMBOL
import at.shared.ATServerAPIDefines.ATQuoteFieldType
import at.feedapi.ActiveTickServerAPI
import at.feedapi.Helpers
import at.shared.ATServerAPIDefines.ATConstituentListType
import at.shared.ATServerAPIDefines.ATBarHistoryType
import at.shared.ATServerAPIDefines.ATSymbolType
import at.shared.ATServerAPIDefines.ATStreamRequestType
import at.utils.jlib.Errors
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.util.*
import kotlin.jvm.JvmStatic

class ATServerAPIExample : Thread() {
    fun PrintUsage() {
        println("ActiveTick Feed Java API")
        println("Available commands:")
        println("-------------------------------------------------------------")
        println("?")
        println("quit")
        println("init [serverHostname] [serverPort] [apiKey] [userid] [password]")
        println("\tserverHostname: activetick1.activetick.com serverPort: 443")
        println("\tapiUserId: valid alphanumeric apiKey, for example EF1C0A768BBB11DFBCB3F923E0D72085")
        println("\tuserid and password: valid account login credentials")
        println("\texample: init activetick1.activetick.com 443 EF1C0A768BBB11DFBCB3F923E0D72085 myuser mypass")
        println("")
        println("getIntradayHistoryBars [symbol] [minutes] [beginTime] [endTime]")
        println("\tminutes: intraday bar minute interval, for example 1-minute, 5-minute bars")
        println("")
        println("getDailyHistoryBars [symbol] [beginTime] [endTime]")
        println("getWeeklyHistoryBars [symbol] [beginTime] [endTime]")
        println("")
        println("getTicks [symbol] [beginTime] [endTime]")
        println("getTicks [symbol] [number of records]")
        println("")
        println("getMarketMovers [symbol] [exchange]")
        println("\tsymbol: \"VL\"=volume, \"NG\"/\"NL\"=net gain/loser, \"PG\"/\"PL\"=percent gain/loser, ")
        println("\texchange: A=Amex, N=NYSE, Q=NASDAQ, U=OTCBB")
        println("\texample: getMarketMovers VL Q")
        println("")
        println("getQuoteDb [symbol]")
        println("getOptionChain [symbol]")
        println("")
        println("subscribeMarketMovers [symbol] [exchange]")
        println("unsubscribeMarketMovers [symbol] [exchange]")
        println("subscribeQuoteStream [symbol]")
        println("unsubscribeQuoteStream [symbol]")
        println("unsubscribeQuotesOnlyQuoteStream [symbol]")
        println("unsubscribeQuotesOnlyQuoteStream [symbol]")
        println("subscribeTradesOnlyQuoteStream [symbol]")
        println("unsubscribeTradesOnlyQuoteStream [symbol]")
        println("")
        println("-------------------------------------------------------------")
        println("Date/time format: YYYYMMDDHHMMSS")
        println("Symbol format: stocks=GOOG, indeces=\$DJI, currencies=#EUR/USD, options=.AAPL--131004C00380000")
        println("-------------------------------------------------------------")
    }

    fun InvalidGuidMessage() {
        println("Warning! \n\tApiUserIdGuid should be 32 characters long and alphanumeric only.")
    }

    /**********************************************************************
     * //processInput
     * Notes:
     * -Process command line input
     */
    fun processInput(userInput: String) {
        val ls = userInput.split(' ','\t','\n','\r')
        val count = ls.size
        if (count > 0 && (ls[0] as String).equals("?", ignoreCase = true)) {
            PrintUsage()
        } else if (count >= 5 && ls[0].equals("init", ignoreCase = true)) {
            val serverHostname = ls[1]
            val serverPort: Int = ls[2].toInt()
            val apiKey = ls[3]
            val userId = ls[4]
            val password = ls[5]
            if (apiKey.length != 32) {
                InvalidGuidMessage()
                return
            }
            val rc = apiSession.Init(ATServerAPIDefines().ATGUID().apply {
                SetGuid(apiKey)
            }, serverHostname, serverPort, userId, password)
            println(
                """
    
    init status: ${if (rc == true) "ok" else "failed"}
    """.trimIndent()
            )
        } else if (count >= 2 && ls[0].equals("getQuoteDb", ignoreCase = true)) {
            getQuoteDb(ls)
        } else if (count == 2 && ls[0].equals("getOptionChain", ignoreCase = true)) {
            getOptionChain(ls)
        } else if (count >= 5 && ls[0].equals("getIntradayHistoryBars", ignoreCase = true)) {
            getIntraHistBars2(ls)
        } else if (count == 6 && (ls[0] as String).equals("getIntradayHistoryBars", ignoreCase = true)) {
            getIntraHistBars(ls)
        } else if (count == 4 && ((ls[0] as String).equals("getDailyHistoryBars", ignoreCase = true) ||
                    ls[0].equals("getWeeklyHistoryBars", ignoreCase = true))
        ) {
            getDailyOrHist(ls)
        } else if (count == 3 && ((ls[0] as String).equals("getDailyHistoryBars", ignoreCase = true) ||
                    (ls[0] as String).equals("getWeeklyHistoryBars", ignoreCase = true))
        ) {
            getDailyOrHist2(ls)
        } else if (count == 5 && (ls[0].equals("getDailyHistoryBars", ignoreCase = true) ||
                    ls[0].equals("getWeeklyHistoryBars", ignoreCase = true))
        ) {
            getDailyOrHist3(ls)
        } else if (count == 4 && ls[0].equals("getTicks", ignoreCase = true)) {
            getTicks0(ls)
        } else if (count == 3 && ls[0].equals("getTicks", ignoreCase = true)) {
            getTicks1(ls)
        } else if (count == 5 && ls[0].equals("getTicks", ignoreCase = true)) {
            getTicks2(ls)
        } else if (count >= 3 && ls[0].equals("getMarketMovers", ignoreCase = true)) {
            getTicks3(ls)
        } else if (count >= 2 && ((ls[0] as String).equals("subscribeMarketMovers", ignoreCase = true) ||
                    (ls[0] as String).equals("unsubscribeMarketMovers", ignoreCase = true))
        ) {
            val strSymbol = ls[1]
            val atSymbol = Helpers.StringToSymbol(strSymbol)
            atSymbol.symbolType = ATSymbolType.TopMarketMovers
            atSymbol.exchangeType = ls[2].toByteArray()[0]
            val requestType = ATServerAPIDefines().ATStreamRequestType()
            requestType.m_streamRequestType = if ((ls[0] as String).equals(
                    "subscribeMarketMovers",
                    ignoreCase = true
                )
            ) ATStreamRequestType.StreamRequestSubscribe else ATStreamRequestType.StreamRequestUnsubscribe
            val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
            lstSymbols.add(atSymbol)
            val request = apiSession!!.GetRequestor().SendATMarketMoversStreamRequest(
                lstSymbols,
                requestType,
                ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong()
            )
            println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
            if (request < 0) {
                println("Error = " + Errors.GetStringFromError(request.toInt()))
            }
        } else if (count >= 2 && ((ls[0] as String).equals("subscribeQuoteStream", ignoreCase = true) ||
                    (ls[0] as String).equals("unsubscribeQuoteStream", ignoreCase = true))
        ) {
            val strSymbols = ls[1]
            val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
            if (!strSymbols.isEmpty() && !strSymbols.contains(",")) {
                val atSymbol = Helpers.StringToSymbol(strSymbols)
                lstSymbols.add(atSymbol)
            } else {
                val symbolTokenizer = StringTokenizer(strSymbols, ",")
                while (symbolTokenizer.hasMoreTokens()) {
                    val atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken())
                    lstSymbols.add(atSymbol)
                }
            }
            val requestType = ATServerAPIDefines().ATStreamRequestType()
            requestType.m_streamRequestType = if ((ls[0] as String).equals(
                    "subscribeQuoteStream",
                    ignoreCase = true
                )
            ) ATStreamRequestType.StreamRequestSubscribe else ATStreamRequestType.StreamRequestUnsubscribe
            val request = apiSession!!.GetRequestor()
                .SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong())
            println("SEND " + request + ": " + ls[0] + " request [" + strSymbols + "]")
            if (request < 0) {
                println("Error = " + Errors.GetStringFromError(request.toInt()))
            }
        } else if (count >= 2 && ((ls[0] as String).equals("subscribeQuotesOnlyQuoteStream", ignoreCase = true) ||
                    (ls[0] as String).equals("unsubscribeQuotesOnlyQuoteStream", ignoreCase = true))
        ) {
            val strSymbols = ls[1]
            val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
            if (!strSymbols.isEmpty() && !strSymbols.contains(",")) {
                val atSymbol = Helpers.StringToSymbol(strSymbols)
                lstSymbols.add(atSymbol)
            } else {
                val symbolTokenizer = StringTokenizer(strSymbols, ",")
                while (symbolTokenizer.hasMoreTokens()) {
                    val atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken())
                    lstSymbols.add(atSymbol)
                }
            }
            val requestType = ATServerAPIDefines().ATStreamRequestType()
            requestType.m_streamRequestType = if ((ls[0] as String).equals(
                    "subscribeQuotesOnlyQuoteStream",
                    ignoreCase = true
                )
            ) ATStreamRequestType.StreamRequestSubscribeQuotesOnly else ATStreamRequestType.StreamRequestUnsubscribeQuotesOnly
            val request = apiSession!!.GetRequestor()
                .SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong())
            println("SEND " + request + ": " + ls[0] + " request [" + strSymbols + "]")
            if (request < 0) {
                println("Error = " + Errors.GetStringFromError(request.toInt()))
            }
        } else if (count >= 2 && ((ls[0] as String).equals("subscribeTradesOnlyQuoteStream", ignoreCase = true) ||
                    (ls[0] as String).equals("unsubscribeTradesOnlyQuoteStream", ignoreCase = true))
        ) {
            val strSymbols = ls[1]
            val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
            if (!strSymbols.isEmpty() && !strSymbols.contains(",")) {
                val atSymbol = Helpers.StringToSymbol(strSymbols)
                lstSymbols.add(atSymbol)
            } else {
                val symbolTokenizer = StringTokenizer(strSymbols, ",")
                while (symbolTokenizer.hasMoreTokens()) {
                    val atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken())
                    lstSymbols.add(atSymbol)
                }
            }
            val requestType = ATServerAPIDefines().ATStreamRequestType()
            requestType.m_streamRequestType = if ((ls[0] as String).equals(
                    "subscribeTradesOnlyQuoteStream",
                    ignoreCase = true
                )
            ) ATStreamRequestType.StreamRequestSubscribeTradesOnly else ATStreamRequestType.StreamRequestUnsubscribeTradesOnly
            val request = apiSession!!.GetRequestor()
                .SendATQuoteStreamRequest(lstSymbols, requestType, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong())
            println("SEND " + request + ": " + ls[0] + " request [" + strSymbols + "]")
            if (request < 0) {
                println("Error = " + Errors.GetStringFromError(request.toInt()))
            }
        } else if (count >= 2 && (ls[0] as String).equals("getMarketHolidays", ignoreCase = true)) {
            val yearsGoingBack = ls[1].toShort()
            val yearsGoingForward = ls[2].toShort()
            val request = apiSession!!.GetRequestor().SendATMarketHolidaysRequest(
                yearsGoingBack,
                yearsGoingForward,
                ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong()
            )
            println("SEND $request:MarketHolidays request for $yearsGoingBack years back and $yearsGoingForward years forward")
            if (request < 0) {
                println("Error = " + Errors.GetStringFromError(request.toInt()))
            }
        }
    }

    private fun getTicks3(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        atSymbol.symbolType = ATSymbolType.TopMarketMovers
        atSymbol.exchangeType = ls[2].toByteArray()[0]
        val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
        lstSymbols.add(atSymbol)
        val request = apiSession!!.GetRequestor()
            .SendATMarketMoversDbRequest(lstSymbols, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong())
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getTicks2(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val beginDateTime = Helpers.StringToATTime(ls[2])
        val numRecords = ls[3].toInt()
        val byteCursorType = ls[4].toInt().toByte()
        val request = apiSession!!.GetRequestor().SendATTickHistoryDbRequest(
            atSymbol, true, true, beginDateTime, numRecords,
            ATServerAPIDefines().ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getTicks1(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val numRecords = ls[2].toInt()
        val request = apiSession!!.GetRequestor().SendATTickHistoryDbRequest(
            atSymbol,
            true,
            true,
            numRecords,
            ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getTicks0(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val beginDateTime = Helpers.StringToATTime(ls[2])
        val endDateTime = Helpers.StringToATTime(ls[3])
        val request = apiSession!!.GetRequestor().SendATTickHistoryDbRequest(
            atSymbol,
            true,
            true,
            beginDateTime,
            endDateTime,
            ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getDailyOrHist3(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val beginDateTime = Helpers.StringToATTime(ls[2])
        val numRecords = ls[3].toInt()
        val byteCursorType = ls[4].toInt().toByte()
        val barHistoryType = if (ls[0].equals(
                "getDailyHistoryBars",
                ignoreCase = true
            )
        ) ATServerAPIDefines().ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) else ATServerAPIDefines().ATBarHistoryType(
            ATBarHistoryType.BarHistoryWeekly
        )
        val request = apiSession!!.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol, barHistoryType, 0.toShort(), beginDateTime, numRecords,
            ATServerAPIDefines().ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getDailyOrHist2(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val numRecords = ls[2].toInt()
        val barHistoryType = if (ls[0].equals(
                "getDailyHistoryBars",
                ignoreCase = true
            )
        ) ATServerAPIDefines().ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) else ATServerAPIDefines().ATBarHistoryType(
            ATBarHistoryType.BarHistoryWeekly
        )
        val request = apiSession!!.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol, barHistoryType, 0.toShort(),
            numRecords, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getDailyOrHist(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val beginDateTime = Helpers.StringToATTime(ls[2])
        val endDateTime = Helpers.StringToATTime(ls[3])
        val barHistoryType = if ((ls[0] as String).equals(
                "getDailyHistoryBars",
                ignoreCase = true
            )
        ) ATServerAPIDefines().ATBarHistoryType(ATBarHistoryType.BarHistoryDaily) else ATServerAPIDefines().ATBarHistoryType(
            ATBarHistoryType.BarHistoryWeekly
        )
        val request = apiSession!!.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol, barHistoryType,
            0.toShort(), beginDateTime, endDateTime, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getQuoteDb(ls: List<String>) {
        val strSymbols = ls[1]
        val lstSymbols: MutableList<ATSYMBOL> = ArrayList()
        if (!strSymbols.isEmpty() && !strSymbols.contains(",")) {
            val atSymbol = Helpers.StringToSymbol(strSymbols)
            lstSymbols.add(atSymbol)
        } else {
            val symbolTokenizer = StringTokenizer(strSymbols, ",")
            while (symbolTokenizer.hasMoreTokens()) {
                val atSymbol = Helpers.StringToSymbol(symbolTokenizer.nextToken())
                lstSymbols.add(atSymbol)
            }
        }
        val lstFieldTypes: MutableList<ATQuoteFieldType> = ArrayList()
        val atServerAPIDefines = ATServerAPIDefines()
        lstFieldTypes.add(atServerAPIDefines.ATQuoteFieldType(ATQuoteFieldType.LastPrice))
        lstFieldTypes.add(atServerAPIDefines.ATQuoteFieldType(ATQuoteFieldType.Volume))
        lstFieldTypes.add(atServerAPIDefines.ATQuoteFieldType(ATQuoteFieldType.LastTradeDateTime))
        lstFieldTypes.add(atServerAPIDefines.ATQuoteFieldType(ATQuoteFieldType.ProfileShortName))
        val request = apiSession!!.GetRequestor()
            .SendATQuoteDbRequest(lstSymbols, lstFieldTypes, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong())
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbols + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getIntraHistBars2(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val minutes = ls[2].toShort()
        val beginDateTime = Helpers.StringToATTime(ls[3])
        val endDateTime = Helpers.StringToATTime(ls[4])
        val barHistoryType = ATServerAPIDefines().ATBarHistoryType(ATBarHistoryType.BarHistoryIntraday)
        val request = apiSession.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol, barHistoryType,
            minutes, beginDateTime, endDateTime, ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getOptionChain(ls: List<String>) {
        val strSymbol = ls[1]
        val constituentType =
            ATServerAPIDefines().ATConstituentListType(ATConstituentListType.ConstituentListOptionChain)
        val request = apiSession!!.GetRequestor().SendATConstituentListRequest(
            constituentType,
            strSymbol.toByteArray(),
            ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT.toLong()
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    private fun getIntraHistBars(ls: List<String>) {
        val strSymbol = ls[1]
        val atSymbol = Helpers.StringToSymbol(strSymbol)
        val minutes = ls[2].toShort()
        val beginDateTime = Helpers.StringToATTime(ls[3])
        val numRecords = ls[4].toInt()
        val byteCursorType = ls[5].toInt().toByte()
        val barHistoryType = ATServerAPIDefines().ATBarHistoryType(ATBarHistoryType.BarHistoryIntraday)
        val request = apiSession.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol, barHistoryType, minutes, beginDateTime, numRecords,
            ATServerAPIDefines().ATCursorType(byteCursorType), ActiveTickServerAPI.DEFAULT_REQUEST_TIMEOUT
        )
        println("SEND " + request + ": " + ls[0] + " request [" + strSymbol + "]")
        if (request < 0) {
            println("Error = " + Errors.GetStringFromError(request.toInt()))
        }
    }

    override fun run() {
        serverapi = ActiveTickServerAPI()
        apiSession = APISession(serverapi)
        serverapi.ATInitAPI()
        val br = BufferedReader(InputStreamReader(System.`in`))
        while (true) {
            try {
                val line = br.readLine()
                if (line.isNotEmpty()) {
                    if (line.startsWith("quit")) break
                    processInput(line)
                }
            } catch (e: IOException) {
                println("IO error trying to read your input!")
            }
        }
        apiSession.UnInit()
        serverapi.ATShutdownAPI()
    }

    companion object {
        lateinit var serverapi: ActiveTickServerAPI
        lateinit var apiSession: APISession
        @JvmStatic
        fun main(args: Array<String>) {
            val apiExample = ATServerAPIExample()
        }
    }

    init {
        PrintUsage()
        start() //get into the run method		
    }
}