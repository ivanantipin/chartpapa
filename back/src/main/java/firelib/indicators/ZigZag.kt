package firelib.indicators

import firelib.core.domain.Ohlc

class ZigZag (val ratio : Double ){

    var data = mutableListOf<Ohlc>()

    var uptrend = false

    var lastExtremePoint = 0

    val liseners = mutableListOf<(List<Ohlc>)->Unit>()


    fun addListener(listener: (zig : List<Ohlc> )->Unit){
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
        liseners.forEach({ it(data.subList(0, lastExtremePoint + 1)) })
        data = data.subList(lastExtremePoint, data.size).toMutableList()
        lastExtremePoint = data.size - 1
        uptrend = !uptrend
    }
}