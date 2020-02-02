package firelib.core.config

data class OptResourceParams(val threadCount : Int, val batchSize : Int)

interface OptResourceStrategy {
    fun getParams(variations : Int) : OptResourceParams
}


class ManualOptResourceStrategy(val threadsNumber : Int, val batchSize : Int) : OptResourceStrategy{
    override fun getParams(variations: Int): OptResourceParams {
        return OptResourceParams(threadsNumber,batchSize)
    }
}
