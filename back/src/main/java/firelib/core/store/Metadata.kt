package firelib.core.store

import firelib.core.domain.InstrId

data class Metadata(val instrIds: List<InstrId>, val period: Int)