package firelib.core.store

import firelib.core.store.reader.SimplifiedReader

interface ReaderFactory{
    fun makeReader(security: String) : SimplifiedReader
}