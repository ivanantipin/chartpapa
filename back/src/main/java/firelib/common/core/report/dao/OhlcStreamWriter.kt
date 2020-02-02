package firelib.common.core.report.dao

import firelib.store.MdDao
import firelib.common.core.report.SqlUtils
import firelib.domain.Ohlc
import java.nio.file.Path

class OhlcStreamWriter(val path: Path) {
    val ds = SqlUtils.getDsForFile(path.toAbsolutePath().toString())
    val mdDao = MdDao(ds)
    fun insertOhlcs(secName: String, ohlcs: List<Ohlc>) {
        mdDao.insertOhlc(ohlcs, secName)
    }
}