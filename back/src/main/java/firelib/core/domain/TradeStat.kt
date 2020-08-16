package firelib.core.domain




class TradeStat(val price: Double, val side: Side) {

    var maxHoldingPrice: Double = price
    var minHoldingPrice: Double = price

    val factors = mutableListOf<Pair<String,Double>>()
    val discreteFactors = mutableListOf<Pair<String, Int>>()

    fun MAE(): Double {
        return if (this.side == Side.Sell) price - maxHoldingPrice else minHoldingPrice - price
    }

    fun MFE(): Double {
        return if (side == Side.Sell) price - minHoldingPrice else maxHoldingPrice - price
    }


    fun onPrice(pr: Double) {
        minHoldingPrice = Math.min(pr, minHoldingPrice)
        maxHoldingPrice = Math.max(pr, maxHoldingPrice)
    }

    fun addFactor(name: String, value: Double) {
        factors += name to value
    }

    fun addFactor(name: String, value: Int) {
        discreteFactors += name to value
    }


}