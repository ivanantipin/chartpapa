package firelib.core.backtest.tradegate

import java.util.*

class StopOBook : OrderStrategy{
    override fun sellOrdering  (): Comparator<OrderKey> = OrderKeyInBookComparator().reversed()
    override fun buyOrdering  (): Comparator<OrderKey> = OrderKeyInBookComparator()

    override fun buyMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice < (bid + ask)/2
    override fun sellMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice > (bid + ask)/2

    override fun buyPrice(bid : Double, ask : Double, ordPrice : Double) = ask
    override fun sellPrice(bid : Double, ask : Double, ordPrice : Double) = bid

}