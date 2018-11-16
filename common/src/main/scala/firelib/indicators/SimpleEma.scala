package firelib.indicators

class SimpleEma(val period: Int) {
    var koeff = 2.0 / (period + 1)
    var ema: Double = 0
    def add(va: Double): Unit = {
        ema = ema * (1 - koeff) + va * koeff
    }
}
