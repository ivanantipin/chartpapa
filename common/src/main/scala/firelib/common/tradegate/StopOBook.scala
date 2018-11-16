package firelib.common.tradegate

import java.util.Comparator

trait StopOBook {
    def sellOrdering  : Comparator[OrderKey] = new OrderKeyInBookComparator().reversed()
    def buyOrdering  : Comparator[OrderKey] = new OrderKeyInBookComparator

    def buyMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice < (bid + ask)/2
    def sellMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice > (bid + ask)/2

    def buyPrice(bid : Double, ask : Double, ordPrice : Double) = ask
    def sellPrice(bid : Double, ask : Double, ordPrice : Double) = bid

}