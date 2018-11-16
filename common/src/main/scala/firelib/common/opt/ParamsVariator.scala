package firelib.common.opt

import scala.collection.mutable

/**
 * iterator over all combinations of optimized variables
 * returns map [string,int] every iteration. Keys are parameter names.
 * @param optParams
 */
class ParamsVariator(optParams: Seq[OptimizedParameter]) {

    val optimizationParams = optParams.map(op => op.getVariations)
    var idx = 0
    val dividers = new Array[Int](optimizationParams.length)
    var combinations = 1
    for (i <- optimizationParams.length - 1 to 0 by -1) {
        dividers(i) = combinations
        combinations *= optimizationParams(i).length
    }

    def hasNext() : Boolean = idx < combinations

    def next: Map[String, Int] = {
        assert(hasNext(),"no available combinations any more!!")
        var ret = new mutable.HashMap[String, Int]()
        var idxTmp = idx
        for (i <- 0 until dividers.length) {
            ret += optParams(i).name -> optimizationParams(i)(idxTmp / dividers(i))
            idxTmp %= dividers(i)
        }
        idx += 1
        return ret.toMap
    }

}
