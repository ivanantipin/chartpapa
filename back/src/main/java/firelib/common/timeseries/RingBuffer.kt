package firelib.common.timeseries


class RingBuffer<T>(val length: Int, val func: (i: Int) -> T) {

    val data: ArrayList<T> = ArrayList(length)
    var count = 0
    var head = 0

    init {
        for (i in 0..length) {
            data.add(func(i))
        }
    }

    operator fun get(idx: Int): T = data[calcIdx(idx)]

    private fun calcIdx(idx: Int): Int {
        assert(idx >= 0 && idx < data.size)
        return (head - idx + length) % length
    }

    operator fun set(idx: Int, value: T) {
        data[calcIdx(idx)] = value
    }

    fun add(t: T): Unit {
        head = (head + 1) % length
        count += 1
        set(0, t)
    }
}