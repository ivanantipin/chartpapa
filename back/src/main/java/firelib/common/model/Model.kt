package firelib.common.model

import firelib.common.ordermanager.OrderManager
import firelib.common.ordermanager.OrderManagerImpl
import firelib.common.ordermanager.flattenAll

/**

 */
open class Model(val context: ModelContext, val properties: Map<String, String>) {

    val oms = makeOrderManagers(context)

    fun properties(): Map<String, String>{
        return properties
    }

    fun name(): String{
        return this.javaClass.name
    }

    fun orderManagers(): List<OrderManager>{
        return oms
    }

    open fun update(){}

    fun modelContext() : ModelContext{
        return context
    }

    /**
     * called after backtest end
     */
    open fun onBacktestEnd(){
        orderManagers().forEach {it.flattenAll()}
    }


    fun makeOrderManagers(ctx: ModelContext): List<OrderManager> {
        return ctx.config.instruments.map { OrderManagerImpl(ctx.tradeGate, ctx.timeService, it.ticker,20, it.instrId) }
    }

}