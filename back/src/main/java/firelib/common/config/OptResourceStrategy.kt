package firelib.common.config

import kotlin.math.*

data class OptResourceParams(val threadCount : Int, val batchSize : Int)

interface OptResourceStrategy {
    fun getParams(variations : Int) : OptResourceParams
}

class DefaultOptResourceStrategy : OptResourceStrategy{

    override fun getParams(variations: Int): OptResourceParams {
        var proc = Runtime.getRuntime().availableProcessors();
        proc = max(proc - 1,1)
        proc = min(proc,3)
        val batch = ceil((variations/proc.toDouble())).toInt()
        return OptResourceParams(proc,batch)
    }
}

class ManualOptResourceStrategy(val threadsNumber : Int, val batchSize : Int) : OptResourceStrategy{
    override fun getParams(variations: Int): OptResourceParams {
        return OptResourceParams(threadsNumber,batchSize)
    }
}
