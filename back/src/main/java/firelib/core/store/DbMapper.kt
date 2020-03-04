package firelib.core.store

import firelib.core.InstrumentMapper
import firelib.core.domain.InstrId
import firelib.core.report.dao.GeGeWriter
import org.slf4j.LoggerFactory

class DbMapper(val readWrite: GeGeWriter<InstrId>, filter: (InstrId) -> Boolean = { true }) : InstrumentMapper {

    val log = LoggerFactory.getLogger(javaClass)

    val symbols = readWrite.read().filter(filter).associateBy { it.code.toLowerCase() }

    init {
        log.info("loaded ${symbols.size} instruments ")
    }

    override fun invoke(p1: String): InstrId {
        return symbols[p1.toLowerCase()]!!
    }
}

fun trqMapperWriter(): GeGeWriter<InstrId> {
    return GeGeWriter(
        "trq_instruments",
        GlobalConstants.metaDb,
        InstrId::class,
        listOf("code", "board")
    )
}

fun finamMapperWriter(): GeGeWriter<InstrId> {
    return GeGeWriter(
        "finam_instruments",
        GlobalConstants.metaDb,
        InstrId::class,
        listOf("id", "code", "market")
    )
}

fun dummyMapperWriter(): GeGeWriter<InstrId> {
    return GeGeWriter(
        "dummy_instruments",
        GlobalConstants.metaDb,
        InstrId::class,
        listOf("code")
    )
}



fun populateMapping(writer: GeGeWriter<InstrId>, func : ()->List<InstrId>) : DbMapper {
    val lst = writer.read()
    if (lst.isEmpty()) {
        println("mapping is empty populating")
        val symbols = func()
        writer.write(symbols)
        println("inserted ${symbols.size} instruments")
    }
    return DbMapper(writer, { true })
}

