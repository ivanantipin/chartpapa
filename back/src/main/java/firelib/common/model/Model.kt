package firelib.common.model

import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.ordermanager.flattenAll

/**

 */
interface Model {

    fun properties(): Map<String, String>

    fun name(): String{
        return this.javaClass.name
    }

    fun orderManagers(): List<OrderManager>

    fun update()

    /**
     * called after backtest end
     */
    fun onBacktestEnd(){
        orderManagers().forEach {it.flattenAll()}
    }


    fun makeOrderManagers(ctx: ModelContext): List<OrderManager> {
        return ctx.instruments
                .map { OrderManagerImpl(ctx.tradeGate, ctx.timeService, it) }
    }

}