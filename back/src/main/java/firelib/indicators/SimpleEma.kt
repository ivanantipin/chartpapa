package firelib.indicators

class SimpleEma(val period: Int) {
    var koeff = 2.0 / (period + 1)
    var ema: Double = 0.0
    fun add(va: Double): Unit {
        ema = ema * (1 - koeff) + va * koeff
    }
}