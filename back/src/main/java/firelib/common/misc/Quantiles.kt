package firelib.common.misc

import firelib.common.misc.navmap.IndexedTreeSet
import java.util.*


class Quantiles<T>(val window: Int) {
    val set = IndexedTreeSet<T>()

    val lst = LinkedList<T>()

    fun add(metric: T) {
        set.add(metric)
        lst.add(metric)
        while (lst.size > window) {
            set.remove(lst.poll())
        }
    }

    fun getQuantile(metric: T): Double {
        val ceiling = set.ceiling(metric)
        if (ceiling != null) {
            return set.entryIndex(ceiling) / set.size.toDouble()
        }
        return Double.NaN
    }
}

fun main() {

    val set = Quantiles<Double>(10000)


    val arr = Random().doubles(1000_000, 0.0, 2.0).toArray()

    arr.forEachIndexed({ idx, it ->
        set.add(it)

        if (idx % 1000 == 0) {
            println(set.set.size)
            println(set.getQuantile(1.5))
        }
    })


}