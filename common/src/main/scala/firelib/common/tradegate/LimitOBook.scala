package firelib.common.tradegate

import java.util.Comparator

trait LimitOBook {
    def sellOrdering  : Comparator[OrderKey] = new OrderKeyInBookComparator()
    def buyOrdering  : Comparator[OrderKey] = new OrderKeyInBookComparator().reversed()

    def buyMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice >= ask
    def sellMatch(bid : Double, ask : Double, ordPrice : Double) = ordPrice <= bid

    def buyPrice(bid : Double, ask : Double, ordPrice : Double) = ordPrice
    def sellPrice(bid : Double, ask : Double, ordPrice : Double) = ordPrice

}
