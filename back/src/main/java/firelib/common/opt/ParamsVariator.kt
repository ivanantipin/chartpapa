package firelib.common.opt

/**
 * iterator over all combinations of optimized variables
 * returns map <string,int> every iteration. Keys are parameter names.
 * @param optParams
 */
class ParamsVariator(val optParams: List<OptimizedParameter>) : Iterator<Map<String,Int>> {

    var nxt : Map<String,Int>? = null

    override fun next(): Map<String, Int> {
        val ret = nxt
        nxt = next0()
        return ret!!
    }

    init {
        nxt = next0()
    }

    fun next0(): Map<String, Int>? {
        if (!optParams[0].next()) {
            optParams[0].reset();
            for (i in 1 until optParams.size) {
                if (optParams[i].next()) {
                    break;
                } else {
                    if(i == optParams.size - 1){
                        return null;
                    }
                    optParams[i].reset()
                }
            }
        }
        return optParams.associateBy({ it.name }, { it.value })
    }

    override fun hasNext(): Boolean {
        return nxt != null
    }



    fun main(args: Array<String>): Unit {
        val variator = ParamsVariator(listOf(OptimizedParameter("A", 1, 3),
                OptimizedParameter("B", 1, 4)
        ))

        for( i in variator){
            print(i)
        }
    }

    fun combinations(): Int {
        return optParams.map { it.count() }.fold(1, {a,b->a*b})
    }

}