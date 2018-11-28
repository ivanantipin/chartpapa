package firelib.common.interval

import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneOffset


enum class Interval(val durationMs: Long) {

    Ms100(100),
    Sec1(1000),
    Sec5(5000),
    Sec10(10 * 1000),
    Sec30(30 * 1000),
    Min1(1 * 60 * 1000),
    Min5(5 * 60 * 1000),
    Min10(10 * 60 * 1000),
    Min15(15 * 60 * 1000),
    Min30(30 * 60 * 1000),
    Min60(60 * 60 * 1000),
    Min120(120 * 60 * 1000),
    Min240(240 * 60 * 1000),
    Day(1440 * 60 * 1000),
    Week(1440 * 60 * 1000*7);

    fun resolveFromMs(ms: Long): Interval? {
        return values().find { it.durationMs == ms }
    }
    fun resolveFromName(name: String): Interval? {
        return values().find { it.name == name }
    }
    val duration = Duration.ofMillis(durationMs)
    fun roundTime(dt: Instant): Instant = Instant.ofEpochMilli(truncTime((dt.toEpochMilli())))
    fun ceilTime(dt: Instant): Instant {
        var ret = (dt.toEpochMilli() / durationMs) * durationMs
        if (dt.toEpochMilli() % durationMs > 0) {
            ret += durationMs
        }
        return Instant.ofEpochMilli(ret)
    }
    fun ceilTime(dt: LocalDateTime): LocalDateTime {
        return LocalDateTime.ofInstant(ceilTime(dt.toInstant(ZoneOffset.UTC)), ZoneOffset.UTC)
    }
    fun truncTime(epochMs: Long): Long = (epochMs / durationMs) * durationMs

}

