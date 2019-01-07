package com.funstat.store

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule

import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.jdbc.datasource.DataSourceTransactionManager
import org.springframework.transaction.support.TransactionTemplate
import org.sqlite.SQLiteDataSource
import java.io.ByteArrayOutputStream
import java.io.IOException

class GenericDaoImpl(internal val ds: SQLiteDataSource) : GenericDao {

    private val manager = DataSourceTransactionManager(ds)

    val mapper = ObjectMapper().registerModule(KotlinModule())

    fun write(obj: Any): String {
        val str = ByteArrayOutputStream()
        try {
            mapper.writer().writeValue(str, obj)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
        return String(str.toByteArray())
    }

    fun <T> deser(str: String, clazz: Class<T>): T {
        try {
            return mapper.readValue(str, clazz)
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    @Synchronized
    override fun <T> saveGeneric(type: String, obj: List<T>, keyMapper: (T)->String) {

        ensureExistGeneric(type)

        println("inserting " + obj.size + " into " + type)

        val data = obj.map { ob ->
            mapOf("KEY" to keyMapper(ob), "JSON" to write(ob!!))
        }.toTypedArray()


        saveInTransaction("insert or replace into $type (key,json) values (:KEY,:JSON)", data as Array<Map<String,Any>>)

    }

    private fun ensureExistGeneric(type: String) {
        JdbcTemplate(ds).execute("create table if not exists $type (json varchar, key varchar primary key )")
    }


    private fun saveInTransaction(sql: String, data: Array<Map<String,Any>>) {
        val template = TransactionTemplate(manager)
        template.execute<Any> { status ->NamedParameterJdbcTemplate(ds).batchUpdate(sql, data)}
    }

    @Synchronized
    override fun <T> readGeneric(tableName: String, clazz: Class<T>): List<T> {
        ensureExistGeneric(tableName)
        return JdbcTemplate(ds).query("select * from $tableName") { rs, rowNum -> deser(rs.getString("json"), clazz) }
    }
}
