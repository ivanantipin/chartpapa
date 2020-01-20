package firelib.common.report

import firelib.common.report.SqlTypeMapper.mapType
import java.math.BigDecimal
import java.time.Instant
import kotlin.reflect.KType
import kotlin.reflect.jvm.reflect


object SqlTypeMapper{

    fun mapType(retType: KType): String {
        if (retType.classifier == String::class) {
            return "VARCHAR"
        }
        if (retType.classifier == Double::class) {
            return "DOUBLE PRECISION"
        }
        if (retType.classifier == Int::class) {
            return "INT"
        }
        if (retType.classifier == Long::class) {
            return "INT"
        }

        if (retType.classifier == BigDecimal::class) {
            return "DOUBLE PRECISION"
        }


        if (retType.classifier == Instant::class) {
            return "TIMESTAMPTZ"
        }

        throw RuntimeException("not supported type $retType")
    }

    fun fromDb(retType: KType): (Any)->(Any) {
        if (retType.classifier == String::class) {
            return {it}
        }
        if (retType.classifier == Double::class) {
            return {it}
        }
        if (retType.classifier == Int::class) {
            return {it}
        }
        if (retType.classifier == Long::class) {
            return { (it as Number).toLong() }
        }

        if (retType.classifier == BigDecimal::class) {
            return { (it as Number).toDouble().toBigDecimal() }
        }

        if (retType.classifier == Instant::class) {
            return {Instant.ofEpochMilli ((it as Int).toLong()*1000)}
        }

        throw RuntimeException("not supported type $retType")
    }

    fun toDb(retType: KType): (Any)->(Any) {
        if (retType.classifier == String::class) {
            return {it}
        }
        if (retType.classifier == Double::class) {
            return {it}
        }
        if (retType.classifier == BigDecimal::class) {
            return {(it as BigDecimal).toDouble()}
        }

        if (retType.classifier == Int::class) {
            return {it}
        }
        if (retType.classifier == Long::class) {
            return { it  }
        }

        if (retType.classifier == Instant::class) {
            return {(it as Instant).toEpochMilli()/1000}
        }

        throw RuntimeException("not supported type $retType")
    }




}

class ColDef<I,T>(val name : String, val extract : (I)->T, val typeOverride : KType? = null){
    fun getSqlType(): String{
        return mapType(typeOverride ?: extract.reflect()!!.returnType)
    }

}

fun <I,T> makeMetric(name : String, funct: (I)->T): ColDef<I,T> {
    return ColDef<I,T>(name,funct)
}


fun <I> getHeader(colsDef : Array<ColDef<I,out Any>>): Map<String,String> {
    return colsDef.associateBy ({it.name},{it.getSqlType()})
}

fun <I> toMapForSqlUpdate(t : I, colsDef : Array<ColDef<I,out Any>>) : Map<String,Any> {
    return colsDef.associateBy ({ it.name}, { it.extract(t) })
}