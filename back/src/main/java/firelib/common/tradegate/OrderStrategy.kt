package firelib.common.tradegate

import java.util.Comparator

interface OrderStrategy {
    fun sellOrdering  (): Comparator<OrderKey>
    fun buyOrdering  (): Comparator<OrderKey>

    fun buyMatch(bid : Double, ask : Double, ordPrice : Double) : Boolean
    fun sellMatch(bid : Double, ask : Double, ordPrice : Double) : Boolean

    fun buyPrice(bid : Double, ask : Double, ordPrice : Double) : Double
    fun sellPrice(bid : Double, ask : Double, ordPrice : Double) : Double
}