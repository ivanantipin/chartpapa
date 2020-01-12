package firelib.common.config

import com.fasterxml.jackson.annotation.JsonIgnore
import com.funstat.domain.InstrId
import firelib.common.reader.SimplifiedReader
import firelib.domain.Ohlc
import java.time.Instant

data class InstrumentConfig(val ticker: String,
                            @get:JsonIgnore
                            val factory : (Instant)->SimplifiedReader,
                            val instrId: InstrId
)

