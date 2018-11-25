package firelib.common.config

import firelib.common.MarketDataType

/**
 *
 * @param ticker - alias of instrument
 * @param path - relative path in dataserver root to csv file
 * @param mdType - ohlc or tick at this moment only
 *
 */
data class InstrumentConfig(val ticker: String, val path: String, val mdType: MarketDataType) {}