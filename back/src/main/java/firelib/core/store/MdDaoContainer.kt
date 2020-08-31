package firelib.core.store

import firelib.core.SourceName
import firelib.core.domain.Interval
import firelib.core.misc.SqlUtils
import org.apache.commons.io.FileUtils
import java.io.File

class MdDaoContainer(val folder: String = GlobalConstants.mdFolder.toString()) {
    val container = SingletonsContainer()

    fun getDao(source: SourceName, interval: Interval): MdDao {
        return container.get("$source/${interval}") {
            val folder = this.folder + "/" + source + "/"
            FileUtils.forceMkdir(File(folder))
            MdDao(SqlUtils.getDsForFile("$folder$interval.db"))
        }
    }

}