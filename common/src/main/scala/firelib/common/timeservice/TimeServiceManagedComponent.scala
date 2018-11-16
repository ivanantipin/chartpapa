package firelib.common.timeservice

import java.time.Instant

trait TimeServiceManagedComponent {

    val timeServiceManaged = new TimeServiceManaged

    class TimeServiceManaged extends TimeService {

        var dtGmt : Instant = Instant.EPOCH

        override def currentTime: Instant = dtGmt
    }

}
