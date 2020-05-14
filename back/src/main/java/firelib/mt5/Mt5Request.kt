package firelib.mt5

data class Mt5Request(
    val action: String = "None",
    val actionType: String = "None",
    val symbol: String = "None",
    val chartTF: String = "None",
    val fromDate: String = "None",
    val toDate: String = "None",
    val id: String = "None",
    val magic: String = "None",
    val volume: String = "None",
    val price: String = "None",
    val stoploss: String = "None",
    val takeprofit: String = "None",
    val expiration: String = "None",
    val deviation: String = "None",
    val comment: String = "None"
)