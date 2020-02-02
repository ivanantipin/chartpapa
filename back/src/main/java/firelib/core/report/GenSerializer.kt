package firelib.core.report

import firelib.core.report.dao.ColDef


fun <I,T> makeMetric(name : String, funct: (I)->T): ColDef<I, T> {
    return ColDef<I, T>(name, funct)
}


fun <I> getHeader(colsDef : Array<ColDef<I, out Any>>): Map<String,String> {
    return colsDef.associateBy ({it.name},{it.getSqlType()})
}

fun <I> toMapForSqlUpdate(t : I, colsDef : Array<ColDef<I, out Any>>) : Map<String,Any> {
    return colsDef.associateBy ({ it.name}, { it.extract(t) })
}