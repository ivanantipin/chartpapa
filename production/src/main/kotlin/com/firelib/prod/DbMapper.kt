package com.firelib.prod

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.core.report.dao.GeGeWriter
import java.nio.file.Path

class DbMapper(path: Path, tableName : String, filter : (InstrId)->Boolean = {true}) :
    InstrumentMapper {

    val readWrite =
        GeGeWriter<InstrId>(
            tableName,
            path,
            InstrId::class
        )
    val symbols = readWrite.read().filter(filter).associateBy { it.code }

    fun updateSymbols(lst : List<InstrId>){
        readWrite.write(lst)
    }


    override fun invoke(p1: String): InstrId {
        return symbols[p1]!!
    }

}