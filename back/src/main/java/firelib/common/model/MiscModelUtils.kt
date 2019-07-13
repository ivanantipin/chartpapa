package firelib.common.model


fun Model.enableFactor(name: String, fact: (Int) -> Double): Unit {
    orderManagers().forEachIndexed { index, orderManager ->
        orderManager.tradesTopic().subscribe {
            it.tradeStat.addFactor(name, fact(index))
        }
    }
}

