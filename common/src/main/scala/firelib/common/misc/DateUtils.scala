package firelib.common.misc

import java.time._
import java.time.format.DateTimeFormatter


object dateUtils extends DateUtils{

}

trait DateUtils {

    val dateStringFormatOfDateUtils = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")

    val nyZoneId = ZoneId.of("America/New_York")
    val londonZoneId = ZoneId.of("Europe/London")
    val moscowZoneId = ZoneId.of("Europe/Moscow")

    def parseAtZone(str : String, zone : ZoneId) : Instant = {
        LocalDateTime.parse(str,dateStringFormatOfDateUtils).atZone(zone).toInstant
    }

    implicit class DblImplicits(vv : Double ) {
        def toStrWithDecPlaces(decPlaces: Int): String = utils.dbl2Str(vv,decPlaces)
    }

    implicit class ParseTimeStandard(str : String ) {
        def parseTimeStandard : Instant  = LocalDateTime.parse(str,dateStringFormatOfDateUtils).toInstant(ZoneOffset.UTC)
        def parseTimeAtZone(zone : ZoneId) : Instant = parseAtZone(str,zone)
    }

    implicit class IntDurations(vv : Int ) {
        def second : Duration  = Duration.ofSeconds(vv)
        def minute : Duration  = Duration.ofMinutes(vv)
        def hour : Duration  = Duration.ofHours(vv)
        def day : Duration  = Duration.ofDays(vv)
    }


    implicit class TimeInstantUtils(that : Instant) {
        def toNyTime : ZonedDateTime = that.atZone(nyZoneId)
        def toLondonTime : ZonedDateTime = that.atZone(londonZoneId)
        def toMoscowTime : ZonedDateTime = that.atZone(moscowZoneId)

        def toStandardString : String = if(that == null) "null" else dateStringFormatOfDateUtils.format(that.atZone(ZoneOffset.UTC))
    }

}


