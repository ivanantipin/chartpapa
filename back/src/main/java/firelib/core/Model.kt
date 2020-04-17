package firelib.core

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
        return this.javaClass.simpleName
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
        return instruments().map {
            OrderManagerImpl(
                tradeGate = ctx.tradeGate,
                timeService = ctx.timeService,
                security = it,
                instrument = context.instrumentMapper(it)!!,
                maxOrderCount = 20,
                modelName = name()
            )
        }
    }

}