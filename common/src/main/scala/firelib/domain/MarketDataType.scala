package firelib.common

sealed case class MarketDataType private (val name: String) {
    override def toString: String = name
}

object MarketDataType {
    val Tick = new MarketDataType("Tick")
    val Ohlc = new MarketDataType("Ohlc")
    val Depth = new MarketDataType("Depth")
}


