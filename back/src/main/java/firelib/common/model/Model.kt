package firelib.common.model

import firelib.common.core.OrderManager
import firelib.common.core.OrderManagerImpl
import firelib.common.core.flattenAll

/**

 */
open class Model(val context: ModelContext, val properties: Map<String, String>) {

    val oms = makeOrderManagers(context)

    fun properties(): Map<String, String> {
        return properties
    }

    fun name(): String {
        return this.javaClass.name
    }

    fun orderManagers(): List<OrderManager> {
        return oms
    }

    open fun update() {}

    fun modelContext(): ModelContext {
        return context
    }

    /**
     * called after backtest end
     */
    open fun onBacktestEnd() {
        orderManagers().forEach { it.flattenAll() }
    }


    fun makeOrderManagers(ctx: ModelContext): List<OrderManager> {
        return ctx.config.instruments.map {
            OrderManagerImpl(
                tradeGate = ctx.tradeGate,
                timeService = ctx.timeService,
                security = it,
                instrument = context.instrumentMapper(it),
                maxOrderCount = 20
            )
        }
    }

}