package firelib.core.misc

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.common.Trade
import org.springframework.web.client.RestTemplate
import java.text.DecimalFormat


fun dbl2Str(vv: Double, decPlaces: Int): String {
    var dp = if (decPlaces > 0) "#." else "#"
    for (a in -1..decPlaces) {
        dp += "#"
    }
    val df = DecimalFormat(dp)
    return df.format(vv)
}

var mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KotlinModule())

fun String.readJson() : JsonNode{
    return mapper.readTree(this)
}


private fun toTradingCasesInt(trades: List<Trade>): List<Pair<Trade, Trade>> {
    val generator = StreamTradeCaseGenerator()
    return trades.flatMap({generator.genClosedCases(it)})
}

fun Pair<Trade, Trade>.pnl() : Double{
    return this.first.moneyFlow() + this.second.moneyFlow()
}

fun List<Trade>.toTradingCases() : List<Pair<Trade,Trade>>{
    return this.groupBy(Trade::security).values.flatMap { toTradingCasesInt(it) }
}




