package firelib.common.tradegate

import java.util.Comparator

class OrderKeyInBookComparator extends Comparator[OrderKey]{
    override def compare(o1: OrderKey, o2: OrderKey): Int = {
        if(o1.price == o2.price){
            o1.id.compareTo(o2.id)
        }else{
            o1.price.compareTo(o2.price)
        }
    }
}
