package firelib.indicators.sequenta

data class Signal(val type: SignalType, val reference: Sequenta.Setup, val successor: Sequenta.Setup? = null) {

    override fun toString(): String {
        return "Signal{" +
                "type=" + type +
                '}'.toString()
    }
}
