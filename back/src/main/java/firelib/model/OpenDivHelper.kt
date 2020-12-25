package firelib.model

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.core.domain.Interval
import firelib.core.misc.SqlUtils
import firelib.core.misc.UtilsHandy
import firelib.core.misc.atUtc
import firelib.core.store.GlobalConstants
import firelib.core.store.MdDaoContainer
import firelib.core.store.MdStorageImpl
import firelib.finam.FinamDownloader
import firelib.model.OpenDivHelper.fetchDivs
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.getForObject
import java.time.LocalDate
import java.time.Month
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter



data class Div(val ticker: String, val lastDayWithDivs: LocalDate, val div: Double, val status : String)

object DivHelper {

    val tSwitch = LocalDate.of(2013, Month.SEPTEMBER, 2)

    fun getDivs(): Map<String, List<Div>> {

        val storage = MdStorageImpl()

        val mdDaoContainer = MdDaoContainer()

        val dao = mdDaoContainer.getDao(FinamDownloader.SOURCE, Interval.Min10)

        var trdDays = dao.queryAll("sber").map { it.endTime.atUtc().toLocalDate()!! }.toSet()

        if(trdDays.isEmpty()){
            UtilsHandy.updateTicker("sber")
            trdDays = dao.queryAll("sber").map { it.endTime.atUtc().toLocalDate()!! }.toSet()
        }

        val dsForFile = SqlUtils.getDsForFile(GlobalConstants.mdFolder.resolve("meta.db").toAbsolutePath().toString())
        val divs = JdbcTemplate(dsForFile).query("select * from dividends", { row, _ ->
            var divDate = LocalDate.ofEpochDay(row.getLong("DT"))!!
            var cntDif = if (divDate.isAfter(tSwitch)) 2 else 0;
            while (cntDif > 0) {
                divDate = divDate.minusDays(1)
                if (trdDays.contains(divDate)) {
                    cntDif--
                }
            }


            Div(row.getString("ticker"), divDate, row.getDouble("div"),"na")
        })
        return divs.groupBy { it.ticker }.mapValues { it.value.sortedBy { div -> div.lastDayWithDivs } }
    }
}


object OpenDivHelper {


    val str = """
	
Dividends	
0	
Id	99437
Instrument_Id	279
TradePlace_Id	7
InstrumentName	"Акрон"
InstrumentCode	"AKRN"
InstrumentImageName	"D3348CBE-00AD-477F-8D31-84B8BBC92A56.png"
InstrumentImageTime	"2020-02-14T15:46:53.473"
PaymentPerUnit	130
PaymentProfitability	2.8285
LastPrice	4596
FixingDate	"2019-03-22T00:00:00"
LastDayCanBuy	"2019-03-20T00:00:00"
PaymentPeriod	null
Status	"A"
"""

    val url = "https://api.open-broker.ru/data/v2.0/corporate_events/" +
            "dividends?" +
            "date_from=2019-03-08&" +
            "rowsOffset=0&" +
            "date_to=&" +
            "rowsCount=200&" +
            "orderBy=FixingDate+&status=&instrument_Id=&search_text="

    fun fetchDivs(dateFrom: LocalDate) : List<Div>{

        val template = RestTemplate()

        val format = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        val str = template.getForObject<String>("https://api.open-broker.ru/data/v2.0/corporate_events/dividends?date_from=${dateFrom.format(format)}&rowsOffset=0&date_to=&rowsCount=10000&orderBy=FixingDate+&status=&instrument_Id=&search_text=")

        var mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KotlinModule())

        val tree = mapper.readTree(str)

        val jsonNode = tree["Dividends"]

        return jsonNode.map {
            Div(ticker = it["InstrumentCode"]!!.textValue(),
                div = it["PaymentPerUnit"]!!.doubleValue(),
                lastDayWithDivs = ZonedDateTime.parse(it["LastDayCanBuy"]?.textValue()!!).toLocalDate(),
                status = it["Status"].textValue()
            )
        }.sortedBy { it.lastDayWithDivs }
    }
}

fun main() {

    val allDivs = fetchDivs(LocalDate.now().minusDays(50))

    allDivs.forEach {
        println(it)
    }


//    val writer = GeGeWriter<Div>(GlobalConstants.metaDb, Div::class, listOf("ticker", "LastDayWithDivs"), "open_divs")
//
//    writer.write(allDivs)
}