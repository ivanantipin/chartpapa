package firelib.core.report.dao

import firelib.core.store.MdDao
import firelib.core.report.SqlUtils
import firelib.core.domain.Ohlc
import java.nio.file.Path

class OhlcStreamWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val mdDao = MdDao(ds)
    fun insertOhlcs(secName: String, ohlcs: List<Ohlc>) {
        mdDao.insertOhlc(ohlcs, secName)
    }
}