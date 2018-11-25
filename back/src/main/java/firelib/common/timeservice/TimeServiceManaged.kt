package firelib.common.timeservice

import java.time.Instant

class TimeServiceManaged : TimeService {

    var dtGmt : Instant = Instant.EPOCH

    fun updateTime(time : Instant){
        dtGmt = time
    }

    override fun currentTime(): Instant = dtGmt
}
