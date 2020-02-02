package firelib.core

import firelib.common.reader.SimplifiedReader

interface ReaderFactory{
    fun makeReader(security: String) : SimplifiedReader
}