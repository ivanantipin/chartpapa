package firelib.model

import firelib.core.OrderManager
import firelib.core.OrderManagerImpl
import firelib.core.flattenAll
import org.slf4j.LoggerFactory
import java.time.Instant

/**

 */
open class Model(val context: ModelContext, val properties: Map<String, String>) {

    val log = LoggerFactory.getLogger(javaClass)

    val oms = makeOrderManagers(context)

    fun properties(): Map<String, String> {
        return properties
    }

    fun logRealtime(msg : ()->String){
        if(Instant.now().toEpochMilli() - currentTime().toEpochMilli() < 60*60*1000 ){
            log.info(msg())
        }
    }

    fun name(): String {
        return this.javaClass.name
    }

    fun orderManagers(): List<OrderManager> {
        return oms
    }


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
                instrument = context.instrumentMapper(it)!!,
                maxOrderCount = 20
            )

        }
    }

}