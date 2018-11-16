package firelib.common.config

case class OptResourceParams(threadCount : Int, batchSize : Int)

trait OptResourceStrategy {
    def getParams(variations : Int) : OptResourceParams
}

class DefaultOptResourceStrategy extends OptResourceStrategy{

    override def getParams(variations: Int): OptResourceParams = {
        var proc = Runtime.getRuntime().availableProcessors();
        proc = math.max(proc - 1,1)
        proc = math.min(proc,3)
        val batch = (variations/proc.toDouble).ceil.toInt
        new OptResourceParams(proc,batch)
    }
}

class ManualOptResourceStrategy(val threadsNumber : Int, val batchSize : Int) extends OptResourceStrategy{
    override def getParams(variations: Int): OptResourceParams = {
        new OptResourceParams(threadsNumber,batchSize)
    }
}

