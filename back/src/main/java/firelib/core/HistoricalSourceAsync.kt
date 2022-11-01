package firelib.core

import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import kotlinx.coroutines.flow.Flow
import java.time.LocalDateTime

interface HistoricalSourceAsync{
    suspend fun symbols(): List<InstrId>
    suspend fun load(instrId: InstrId, interval: Interval): Flow<Ohlc>
    suspend fun load(instrId: InstrId, dateTime: LocalDateTime, interval: Interval): Flow<Ohlc>
    fun getName(): SourceName
}