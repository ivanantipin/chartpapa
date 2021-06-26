import at.feedapi.ActiveTickServerAPI
import at.feedapi.Helpers
import at.shared.ATServerAPIDefines


fun main() {
    val serverapi = ActiveTickServerAPI()
    val apiSession = APISession(serverapi)
    serverapi.ATInitAPI()

    apiSession.Init(ATServerAPIDefines().ATGUID().apply {
        SetGuid("79a369a2add34a6c98d7e069d7aedfc4")
    }, "activetick1.activetick.com", 443, "iaa1981", "PCZ7f!DdXJvy6TG")

    val apiDefines = ATServerAPIDefines()

    val beginTime = apiDefines.SYSTEMTIME().apply {
        year = 2021
        month = 4
        day = 1
    }

    val endTime = apiDefines.SYSTEMTIME().apply {
        year = 2021
        month = 6
        day = 24
    }

    while (true){
        val barHistoryType = apiDefines.ATBarHistoryType(ATServerAPIDefines.ATBarHistoryType.BarHistoryIntraday)

        val atSymbol = Helpers.StringToSymbol("NINE")



        val request = apiSession.GetRequestor().SendATBarHistoryDbRequest(
            atSymbol,
            barHistoryType,
            10.toShort(),
            beginTime,
            endTime,
            200
        )
        println(request)
        Thread.sleep(10000)
    }




}