package firelib.store

import firelib.domain.InstrId

data class Metadata(val instrIds: List<InstrId>, val period: Int)