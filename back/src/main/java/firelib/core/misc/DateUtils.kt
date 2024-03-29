package firelib.core.misc

import java.time.*
import java.time.format.DateTimeFormatter


val dateStringFormatOfDateUtils = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

val dateFormat = DateTimeFormatter.ofPattern("yyyy.MM.dd")


fun parseAtZone(str : String, zone : ZoneId) : Instant {
    return LocalDateTime.parse(str,dateStringFormatOfDateUtils).atZone(zone).toInstant()
}


fun Double.toStrWithDecPlaces(decPlaces: Int): String = dbl2Str(this,decPlaces)

fun String.parseTimeStandard (): Instant  = LocalDateTime.parse(this,dateStringFormatOfDateUtils).toInstant(ZoneOffset.UTC)
fun String.parseTimeAtZone(zone : ZoneId) : Instant = parseAtZone(this,zone)

fun Long.second (): Duration  = Duration.ofSeconds(this)
fun Long.minute (): Duration  = Duration.ofMinutes(this)
fun Long.hour (): Duration  = Duration.ofHours(this)
fun Long.day (): Duration  = Duration.ofDays(this)

val nyZoneId = ZoneId.of("America/New_York")
val londonZoneId = ZoneId.of("Europe/London")
val moscowZoneId = ZoneId.of("Europe/Moscow")

fun Instant.atUtc (): LocalDateTime = LocalDateTime.ofInstant(this,ZoneOffset.UTC)
fun Instant.atMoscow (): LocalDateTime = LocalDateTime.ofInstant(this, moscowZoneId)
fun Instant.atLondon (): LocalDateTime = LocalDateTime.ofInstant(this, londonZoneId)
fun Instant.atNy (): LocalDateTime = LocalDateTime.ofInstant(this, nyZoneId)

fun Instant.toStandardString (): String = if(this == null) "null" else dateStringFormatOfDateUtils.format(this.atZone(ZoneOffset.UTC))

fun Instant.toStringAtMoscow(): String = if(this == null) "null" else dateStringFormatOfDateUtils.format(this.atMoscow())



fun LocalDateTime.toInstantDefault() : Instant = this.toInstant(ZoneOffset.UTC)
fun LocalDateTime.toInstantMoscow() : Instant = this.atZone(moscowZoneId).toInstant()
fun LocalDateTime.toInstantNy() : Instant = this.atZone(nyZoneId).toInstant()
fun LocalDate.toInstantDefault() : Instant = this.atStartOfDay().toInstant(ZoneOffset.UTC)