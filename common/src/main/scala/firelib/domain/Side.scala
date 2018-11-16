package firelib.common


object Side {
    val Sell = new Side("Sell", -1)
    val Buy = new Side("Buy", 1)
    val None = new Side("None", -1)

    def sideForAmt(amt: Int): Side = {
        assert(amt != 0, "no side for zero amount !!")
        if (amt > 0) Buy else Sell
    }

}

sealed case class Side private(val Name: String, val sign: Int) {

    def opposite: Side = {
        this match {
            case Side.None => Side.None
            case Side.Buy => Side.Sell
            case Side.Sell => Side.Buy
        }
    }

    override def toString: String = Name

}


object ModelInitResult {
    val Success = new ModelInitResult("Success")
    val Failure = new ModelInitResult("Failure")
}

sealed case class ModelInitResult private(val name: String) {
    override def toString: String = name
}


