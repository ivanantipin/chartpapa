package firelib.core.domain

import java.time.Instant


open class Tick() : Comparable<Timed>, Timed {

    override fun toString(): String {
        return "Tick(last=$last bid=$bid ask=$ask vol=$vol time=$dtGmt)"
    }

    override fun compareTo(o: Timed): Int {
        return dtGmt.compareTo(o.time())
    }

    override fun time(): Instant {
        return dtGmt
    }

    var ask: Double = Double.NaN
    var bid: Double = Double.NaN
    var last: Double = Double.NaN
    var vol: Int = 0
    var side: Side = Side.None
    var tickNumber: Int = 0
    var dtGmt: Instant = Instant.now()
}