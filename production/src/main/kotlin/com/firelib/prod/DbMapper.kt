package com.firelib.prod

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.core.report.dao.GeGeWriter

class DbMapper(val readWrite: GeGeWriter<InstrId>, filter: (InstrId) -> Boolean = { true }) : InstrumentMapper {

    val symbols = readWrite.read().filter(filter).associateBy { it.code.toLowerCase() }

    override fun invoke(code: String): InstrId {
        require(symbols.containsKey(code.toLowerCase()), {"no symbol in mapper ${code}"})
        return symbols[code.toLowerCase()]!!
    }

}