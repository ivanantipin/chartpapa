package firelib.common

object OrderType {
    val Limit = new OrderType("Limit")
    val Market = new OrderType("Market")
    val Stop = new OrderType("Stop")
}

sealed case class OrderType private (val Name: String)



