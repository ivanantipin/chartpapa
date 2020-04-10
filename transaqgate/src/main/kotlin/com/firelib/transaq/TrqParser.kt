package com.firelib.transaq

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.MapperFeature
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule

object TrqParser{

    val kotlinXmlMapper = XmlMapper(JacksonXmlModule().apply {
        setDefaultUseWrapper(false)
    }).registerKotlinModule()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)


    fun parseTrqMsg(str: String): TrqMsg? {
        //TrqTrades
        if (str.startsWith("<candlekinds")) {
            return kotlinXmlMapper.readValue(str, CandleKinds::class.java)
        }else if (str.startsWith("<trades")) {
            return kotlinXmlMapper.readValue(str, TrqTrades::class.java)
        } else if (str.startsWith("<boards")) {
            return kotlinXmlMapper.readValue(str, Boards::class.java)
        } else if (str.startsWith("<securities")) {
            return kotlinXmlMapper.readValue(str, Securities::class.java)
        } else if (str.startsWith("<candles")) {
            return kotlinXmlMapper.readValue(str, Candles::class.java)
        } else if (str.startsWith("<pits")) {
            println("received pits  , skipped")
        } else if (str.startsWith("<sec_info_upd")) {
            return kotlinXmlMapper.readValue(str, SecInfoUpd::class.java)
        } else if (str.startsWith("<markets")) {
            val idToMarket = mapOf(
                "0" to "Collateral",
                "1" to "MICEX",
                "4" to "FORTS",
                "14" to "MMA",
                "15" to "ETS"
            )

            val ret = kotlinXmlMapper.readValue(str, Markets::class.java)
            return ret.copy(markets = ret.markets.map { it.copy(text = idToMarket.getOrDefault(it.id, "NA")) })
        } else if (str.startsWith("<portfolio_tplus")) {
            return kotlinXmlMapper.readValue(str, TrqPortfolio::class.java)
        }else if (str.startsWith("<orders")) {
            return kotlinXmlMapper.readValue(str, TrqOrders::class.java)
        } else if (str.startsWith("<alltrades")) {
            return kotlinXmlMapper.readValue(str, AllTrades::class.java)
        } else if (str.startsWith("<client")) {
            return kotlinXmlMapper.readValue(str, TrqClient::class.java)
        } else if (str.startsWith("<server_status")) {
            return kotlinXmlMapper.readValue(str, ServerStatus::class.java)
        }else if (str.startsWith("<positions")) {
            return kotlinXmlMapper.readValue(str, Positions::class.java)
        } else {
            println("unparsed message ${str}")
        }
        return null
    }

    fun parseTrqResponse(str: String): TrqResponse {
        return kotlinXmlMapper.readValue(str, TrqResponse::class.java)
    }

}