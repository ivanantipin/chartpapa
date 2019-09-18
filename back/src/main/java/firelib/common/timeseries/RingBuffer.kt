package firelib.common.timeseries


class RingBuffer<T>(val capacity: Int, val func: (i: Int) -> T) {

    val data: Array<Any> =  Array(capacity) {func(0) as Any}
    var count = 0
    var head = 0

    operator fun get(idx: Int): T = data[calcIdx(idx)] as T

    private fun calcIdx(idx: Int): Int {
        require(idx >= 0 && idx < data.size, {"${idx} >= ${capacity}"})
        return (head - idx + capacity) % capacity
    }

    operator fun set(idx: Int, value: T) {
        data[calcIdx(idx)] = value as Any
    }

    fun copyWithAdjustedCapacity(cap : Int): RingBuffer<T> {
        val ret = RingBuffer<T>(cap, func)
        for(i in 0 until capacity){
            ret[i] = this[i]
        }
        return ret
    }

    fun add(t: T): Unit {
        head = (head + 1) % capacity
        count += 1
        set(0, t)
    }
}