package firelib.core.backtest.opt

/**
 * optimized parameter configuration - support only int values
 * @param name - name of parameter
 * @param start - start of range
 *@param end - end of range
 * @param step - step
 */
data class OptimizedParameter(val name: String, val start: Int, val end: Int, val step: Int = 1) {

    var value = start

    fun next() : Boolean {
        value += step
        return value < end
    }

    fun hasNext() : Boolean {
        return value + step < end
    }


    fun reset() {
        value = start;
    }

    fun value() : Int {
        return value
    }

    fun count() : Int {
        return (end - start)/step
    }
}