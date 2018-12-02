package com.funstat

import com.funstat.store.GenericDao

class PersistDescriptor<T>(internal var table: String, internal var clazz: Class<T>, internal var keyExtractor: (T)->String) {

    fun read(mdDao: GenericDao): List<T> {
        return mdDao.readGeneric(table, clazz)
    }

    fun readByKey(dao: GenericDao, key: String): T? {
        return readAsMap(dao)[key]
    }


    fun readAsMap(mdDao: GenericDao): Map<String, T> {
        return mdDao.readGeneric(table, clazz).associateBy({ keyExtractor(it) }, { it })
    }

    fun write(dao: GenericDao, data: List<T>) {
        dao.saveGeneric(table, data, keyExtractor)
    }

    fun writeSingle(dao: GenericDao, data: T) {
        dao.saveGeneric(table, listOf(data), keyExtractor)
    }
}

