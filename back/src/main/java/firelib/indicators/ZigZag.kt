package firelib.indicators

import firelib.core.domain.Ohlc


data class ZigZagEvent(
    val data : List<Ohlc>,
    val extreme : Double,
    val uptrend : Boolean
)

class ZigZag (val ratio : Double ){

    var data = mutableListOf<Ohlc>()

    var uptrend = false

    var lastExtremePoint = 0

    val liseners = mutableListOf<(ZigZagEvent)->Unit>()


    fun addListener(listener: (zig : ZigZagEvent )->Unit){
        liseners += listener
    }

    fun addOhlc(ohlc: Ohlc){
        data.add(ohlc)


        if(uptrend){
            if(ohlc.high > data[lastExtremePoint].high){
                lastExtremePoint = data.size - 1
            }
            val currentExtreme = data[lastExtremePoint].high
            if((currentExtreme - ohlc.low)/currentExtreme > ratio){
                flip()
            }

        }else{
            if(ohlc.low < data[lastExtremePoint].low){
                lastExtremePoint = data.size - 1
            }
            val currentExtreme = data[lastExtremePoint].low
            if(( ohlc.high - currentExtreme )/currentExtreme > ratio){
                flip()
            }
        }
    }

    private fun flip() {
        val subList = data.subList(0, lastExtremePoint + 1)
        val extr = if(uptrend) subList[lastExtremePoint].high else subList[lastExtremePoint].low
        liseners.forEach({
            it(ZigZagEvent(subList, extr, uptrend)) })
        data = data.subList(lastExtremePoint, data.size).toMutableList()
        lastExtremePoint = data.size - 1
        uptrend = !uptrend
    }
}


fun main() {
    val zigZag = ZigZag(0.01)
    val ohs = listOf<Ohlc>(
        Ohlc(open = 2.0, high = 2.0, low = 2.0, close = 2.0),
        Ohlc(open = 1.0, high = 1.0, low = 1.0, close = 1.0),
        Ohlc(open = 1.0, high = 1.5, low = 1.0, close = 1.0),
        Ohlc(open = 1.5, high = 1.5, low = 1.3, close = 1.5)
    )

    zigZag.addListener { println(it) }

    ohs.forEach {
        zigZag.addOhlc(it)
    }

}