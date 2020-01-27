package firelib.common.core

import firelib.common.reader.SimplifiedReader

interface ReaderFactory{
    fun makeReader(security: String) : SimplifiedReader
}