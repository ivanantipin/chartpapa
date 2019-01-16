package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import firelib.common.reader.MarketDataReader
import firelib.domain.Ohlc
import java.time.Instant

/**
 *
 * @param ticker - alias of instrument
 * @param path - relative path in dataserver root to csv file
 *
 */
data class InstrumentConfig(val ticker: String,
                            @get:JsonIgnore
                            val fact : (Instant)->MarketDataReader<Ohlc>) {}

