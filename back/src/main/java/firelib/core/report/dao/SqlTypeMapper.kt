package firelib.core.report.dao

import java.math.BigDecimal
import java.time.Instant
import kotlin.reflect.KType

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

        if (retType.classifier == Boolean::class) {
            return "INT"
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
            return { Instant.ofEpochMilli((it as Int).toLong() * 1000) }
        }
        if (retType.classifier == Boolean::class) {
            return {it}
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

        if (retType.classifier == Boolean::class) {
            return { it  }
        }


        if (retType.classifier == Instant::class) {
            return {(it as Instant).toEpochMilli()/1000}
        }

        throw RuntimeException("not supported type $retType")
    }




}