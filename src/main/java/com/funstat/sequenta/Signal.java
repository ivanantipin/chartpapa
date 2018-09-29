package com.funstat.sequenta;

public class Signal {
    public SignalType type;
    public Sequenta.Setup reference;
    Sequenta.Setup successor;

    public Signal(SignalType type, Sequenta.Setup reference) {
        this.type = type;
        this.reference = reference;
    }

    public Signal(SignalType type, Sequenta.Setup reference, Sequenta.Setup successor) {
        this.type = type;
        this.reference = reference;
        this.successor = successor;
    }

    @Override
    public String toString() {
        return "Signal{" +
                "type=" + type +
                '}';
    }
}
