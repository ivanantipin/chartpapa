package firelib.common.reader

import java.time.Instant

import firelib.common.config.InstrumentConfig
import firelib.domain.Timed

trait ReadersFactory extends ((InstrumentConfig,Instant) => MarketDataReader[Timed])
