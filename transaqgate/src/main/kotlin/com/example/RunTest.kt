package com.example

import com.example.TrqCommandHelper.getLoginCommand
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import com.firelib.Empty
import com.firelib.Str
import com.funstat.domain.InstrId
import com.funstat.finam.FinamDownloader
import org.apache.commons.text.StringEscapeUtils

internal val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
    setDefaultUseWrapper(false)
}).registerKotlinModule()
        .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

fun parse(str: String): Any {
    if (str.startsWith("<candlekinds")) {
        println(kotlinXmlMapper.readValue(str, CandleKinds::class.java))
        println("====================")

    } else if (str.startsWith("<boards")) {
        println(kotlinXmlMapper.readValue(str, Boards::class.java))
        println("====================")

    } else if (str.startsWith("<securities")) {
        println("received securities ${kotlinXmlMapper.readValue(str, Securities::class.java).securities}")

    } else if (str.startsWith("<pits")) {
        println("received pits  , skipped")
    } else if (str.startsWith("<sec_info_upd")) {
        println("raw sec id ${str}")
        println("received sec info upd ${kotlinXmlMapper.readValue(str, SecInfoUpd::class.java)}")
    } else if (str.startsWith("<markets")) {
        println("received markets ${kotlinXmlMapper.readValue(str, Markets::class.java)}")

    } else {
        println("unparsed message ${str}")
        println("====================")
    }
    return ""
}

fun main() {

    //println(kotlinXmlMapper.readValue(str, TrqCommandHelper.AllTrades::class.java))


//    FinamDownloader().symbols().filter { it.code.equals("sber", ignoreCase = true)}.forEach({
//        println(it)
//    })


    fun getPortfolio(client: String): String {
        return "<command id=\"get_portfolio\" client=\"${client}\"/>"
    }


    val client = TransaqGrpcClientExample("localhost", 50051)

    val loginCommand = getLoginCommand("TCNN9972", "L3n2D3", "tr1-demo5.finam.ru", "3939")

    val blockingStub = client.blockingStub

    fun sendCmd(str : String) : String{
        return StringEscapeUtils.unescapeJava(blockingStub.sendCommand(Str.newBuilder().setTxt(str).build()).txt )
    }


    println("login respons ${sendCmd(loginCommand)}")

    while (true) {
        try {


            //seccode=SRH0, shortname=SBRF-3.20, decimals=0, minstep=1, lotsize=1, point_cost=100, bymarket=null, sectype=FUT, MIC=null, ticker=null, currency=null, instrclass=F),

//            val cmd = TrqCommandHelper.getSecurities()
//            val cmd = TrqCommandHelper.subscribe("RIH0", "FUT")
            val cmd = TrqCommandHelper.getHistory("RIH0", "FUT", "2", 10000)

            println("command to send ${cmd}")
            println("response:" + sendCmd(cmd))

            val messages = blockingStub.connect(Empty.newBuilder().build())

            //continuous messages, this call will generally block till the end
            messages.forEachRemaining { str: Str -> println("server message" + parse(StringEscapeUtils.unescapeJava(str.txt))) }
        } catch (e: Exception) {
            e.printStackTrace()
            Thread.sleep(5000)
        }
    }
}