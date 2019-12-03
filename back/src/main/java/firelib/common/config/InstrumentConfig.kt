package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.common.reader.MarketDataReader
import firelib.domain.Ohlc
import java.time.Instant

data class InstrumentConfig(val ticker: String,
                            @get:JsonIgnore
                            val factory : (Instant)->MarketDataReader<Ohlc>)

