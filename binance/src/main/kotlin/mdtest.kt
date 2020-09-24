package com.binance.api.examples

import com.binance.api.client.BinanceApiClientFactory
import com.binance.api.client.BinanceApiRestClient
import com.binance.api.client.domain.market.Candlestick
import com.binance.api.client.domain.market.CandlestickInterval

/**
 * Examples on how to get market data information such as the latest price of a symbol, etc.
 */
object MarketDataEndpointsExample {
    @JvmStatic
    fun main(args: Array<String>) {
        val factory: BinanceApiClientFactory = BinanceApiClientFactory.newInstance()
        val client: BinanceApiRestClient = factory.newRestClient()

//        print(client.bookTickers)

        // Weekly candlestick bars for a symbol

        var start = System.currentTimeMillis() - 1000 * 24L * 3600_000L

        while (start < System.currentTimeMillis()){
            var end = start + 300*60_000
            val candlesticks: List<Candlestick> =
                client.getCandlestickBars("BTCUSDT", CandlestickInterval.ONE_MINUTE, 500, start, end)
            start = end
            println(candlesticks.size)

        }


    }
}