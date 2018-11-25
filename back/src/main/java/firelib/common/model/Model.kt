package firelib.common.model

import firelib.common.ordermanager.OrderManager

/**

 */
interface Model {

    fun properties(): Map<String, String>

    fun name(): String

    fun orderManagers(): List<OrderManager>

    /**
     * called after backtest end
     */
    fun onBacktestEnd()
}