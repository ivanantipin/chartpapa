package com.funstat.store

interface GenericDao {
    fun <T> saveGeneric(type: String, obj: List<T>, keyMapper: (T)->String)

    fun <T> readGeneric(tableName: String, clazz: Class<T>): List<T>
}
