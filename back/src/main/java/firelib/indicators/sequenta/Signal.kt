package firelib.indicators.sequenta

data class Signal(val type: SignalType, val reference: Sequenta.Setup, val successor: Sequenta.Setup? = null) {

    val idx = reference.data().size - 1

    override fun toString(): String {
        return "Signal{" +
                "type=" + type +
                '}'.toString()
    }
}
