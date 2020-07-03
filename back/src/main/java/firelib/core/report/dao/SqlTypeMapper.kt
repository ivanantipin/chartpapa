package firelib.core.report.dao

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.math.BigDecimal
import java.time.Instant
import java.time.LocalDate
import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.createInstance
import kotlin.reflect.jvm.javaType


val jsonMapper = ObjectMapper().apply {
    this.registerModule(KotlinModule())
}


object SqlTypeMapper {

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

        if (retType.classifier == LocalDate::class) {
            return "INT"
        }

        if (retType.classifier == Instant::class) {
            return "TIMESTAMPTZ"
        }

        return "VARCHAR"
    }

    fun fromDb(retType: KType): (Any) -> (Any) {
        if (retType.classifier == String::class) {
            return { it }
        }
        if (retType.classifier == Double::class) {
            return { it }
        }
        if (retType.classifier == Int::class) {
            return { it }
        }

        if (retType.classifier == LocalDate::class) {
            return { LocalDate.ofEpochDay(it as Long) }
        }

        if (retType.classifier == Long::class) {
            return { (it as Number).toLong() }
        }

        if (retType.classifier == BigDecimal::class) {
            return { (it as Number).toDouble().toBigDecimal() }
        }

        if (retType.classifier == Instant::class) {
            return { Instant.ofEpochMilli((it as Number).toLong() * 1000) }
        }
        if (retType.classifier == Boolean::class) {
            return { it }
        }

        return {
            jsonMapper.readValue(it.toString(), (retType.classifier as KClass<*>).java)
        }
    }

    fun toDb(retType: KType): (Any) -> (Any) {
        if (retType.classifier == String::class) {
            return { it }
        }

        if (retType.classifier == Double::class) {
            return { it }
        }
        if (retType.classifier == BigDecimal::class) {
            return { (it as BigDecimal).toDouble() }
        }

        if (retType.classifier == Int::class) {
            return { it }
        }
        if (retType.classifier == Long::class) {
            return { it }
        }

        if (retType.classifier == Boolean::class) {
            return { it }
        }

        if (retType.classifier == LocalDate::class) {
            return { (it as LocalDate).toEpochDay() }
        }

        if (retType.classifier == Instant::class) {
            return { (it as Instant).toEpochMilli() / 1000 }
        }

        return { jsonMapper.writeValueAsString(it) }
    }
}