package firelib.common.tradegate

import java.util.*

class LimitOBook : OrderStrategy{
    override fun sellOrdering  (): Comparator<OrderKey> = OrderKeyInBookComparator()
    override fun buyOrdering  (): Comparator<OrderKey> = OrderKeyInBookComparator().reversed()

    override fun buyMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice >= ask
    override fun sellMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice <= bid

    override fun buyPrice(bid : Double, ask : Double, ordPrice : Double) = ordPrice
    override fun sellPrice(bid : Double, ask : Double, ordPrice : Double) = ordPrice
}