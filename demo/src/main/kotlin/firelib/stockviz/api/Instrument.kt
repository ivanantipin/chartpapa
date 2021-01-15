package firelib.stockviz.api

import java.math.BigDecimal
import javax.validation.Valid
import javax.validation.constraints.Size

data class Instrument(
    val symbolAndExchange: String,
    val symbol: String,
    val exchange: String,
    val metaDiscrete: Map<String, String> = emptyMap(),
    val metaContinuous: Map<String, BigDecimal> = emptyMap()
)

