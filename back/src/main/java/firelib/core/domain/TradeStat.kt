package firelib.core.domain

class TradeStat() {

    val factors = mutableListOf<Pair<String,Double>>()
    val discreteFactors = mutableListOf<Pair<String, Int>>()

    fun addFactor(name: String, value: Double) {
        factors += name to value
    }

    fun addFactor(name: String, value: Int) {
        discreteFactors += name to value
    }
}