package firelib.common.interval

import java.time.{Duration, Instant}

import scala.collection.mutable.ArrayBuffer

trait AllIntervals{
    val intervals = new ArrayBuffer[Interval]()

    val Ms100 = new Interval("Ms100", 100)
    val Sec1 = new Interval("Sec1", 1000)
    val Sec5 = new Interval("Sec5", 5000)
    val Sec10 = new Interval("Sec10", 10 * 1000)
    val Sec30 = new Interval("Sec30", 30 * 1000)
    val Min1 = new Interval("Min1", 1 * 60 * 1000)
    val Min5 = new Interval("Min5", 5 * 60 * 1000)
    val Min10 = new Interval("Min10", 10 * 60 * 1000)
    val Min15 = new Interval("Min15", 15 * 60 * 1000)
    val Min30 = new Interval("Min30", 30 * 60 * 1000)
    val Min60 = new Interval("Min60", 60 * 60 * 1000)
    val Min120 = new Interval("Min120", 120 * 60 * 1000)
    val Min240 = new Interval("Min240", 240 * 60 * 1000)
    val Day = new Interval("Day", 1440 * 60 * 1000)
}



object Interval extends AllIntervals {

    def resolveFromMs(ms: Long) = intervals.find(_.durationMs == ms).get

    def resolveFromName(name: String) = intervals.find(_.name == name).get

}


sealed case class Interval (val name: String, val durationMs: Int) {

    Interval.intervals += this

    val duration = Duration.ofMillis(durationMs)

    def roundTime(dt:Instant): Instant  = Instant.ofEpochMilli(truncTime((dt.toEpochMilli)))

    def ceilTime(dt:Instant): Instant  = {
        var ret = (dt.toEpochMilli/durationMs) * durationMs
        if(dt.toEpochMilli % durationMs > 0){
            ret += durationMs
        }
        Instant.ofEpochMilli(ret)
    }

    def truncTime(epochMs : Long): Long  =  (epochMs/durationMs) * durationMs

    override def toString: String = name
}


