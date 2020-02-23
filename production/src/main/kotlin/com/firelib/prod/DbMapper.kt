package com.firelib.prod

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.core.report.dao.GeGeWriter
import java.nio.file.Path

class DbMapper(val readWrite: GeGeWriter<InstrId>, filter: (InstrId) -> Boolean = { true }) : InstrumentMapper {

    val symbols = readWrite.read().filter(filter).associateBy { it.code }

    override fun invoke(p1: String): InstrId {
        return symbols[p1]!!
    }

}