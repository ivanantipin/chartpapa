package firelib.common.opt

/**
 * iterator over all combinations of optimized variables
 * returns map <string,int> every iteration. Keys are parameter names.
 * @param optParams
 */
class ParamsVariator(val optParams: List<OptimizedParameter>) : Iterator<Map<String,Int>> {


    var firstIter = true



    override fun next(): Map<String, Int> {
        if(firstIter){
            firstIter = false
            return optParams.associateBy({ it.name }, { it.value })
        }
        val idx = optParams.indexOfFirst { it.hasNext() }
        if (idx >= 0) {
            for (i in 0 until idx) {
                optParams[i].reset()
            }
            optParams[idx].next()
        }
        return optParams.associateBy({ it.name }, { it.value })

    }



    override fun hasNext(): Boolean {
        return optParams.indexOfFirst { it.hasNext() } >= 0
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