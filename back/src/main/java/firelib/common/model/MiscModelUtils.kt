package firelib.common.model


fun Model.enableFactor(name: String, fact: () -> String): Unit {
    for (om in orderManagers()) {
        om.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact())
        }
    }
}

