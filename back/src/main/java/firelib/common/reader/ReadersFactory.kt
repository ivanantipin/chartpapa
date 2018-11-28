package firelib.common.reader

import java.time.Instant

import firelib.common.config.InstrumentConfig
import firelib.domain.Timed

interface ReadersFactory : ((InstrumentConfig,Instant) -> MarketDataReader<Timed>)