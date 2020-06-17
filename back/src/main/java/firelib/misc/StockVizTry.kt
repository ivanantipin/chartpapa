package firelib.misc

import org.openapitools.client.apis.PortfoliosApi
import org.openapitools.client.models.NewTrade
import java.time.OffsetDateTime


fun main() {
    val api = PortfoliosApi()


    api.portfoliosAddTradesCreate("TEST", arrayOf(
        NewTrade(tradeId = "0",
            side = "Sell",
            qty = 1.toBigDecimal(),
            closePrice = 1.toBigDecimal(),
            closeTime = OffsetDateTime.now(),
            continuousTags = emptyMap<String,String>(),
            discreteTags = emptyMap<String,String>(),
            openPrice = 1.toBigDecimal(),
            openTime = OffsetDateTime.now(), symbol = "SBER", pnl = 1.toBigDecimal()

            )
    ))
}