package firelib.core.backtest.tradegate

import java.util.*

class OrderKeyInBookComparator : Comparator<OrderKey>{
    override fun compare(o1: OrderKey, o2: OrderKey): Int {
        return  if(o1.price == o2.price){
            o1.id.compareTo(o2.id)
        }else{
            o1.price.compareTo(o2.price)
        }
    }
}