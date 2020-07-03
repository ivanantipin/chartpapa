package firelib

import firelib.core.domain.Interval
import firelib.core.domain.ret
import firelib.core.misc.atMoscow
import firelib.core.misc.toStrWithDecPlaces
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import firelib.core.store.finamMapperWriter
import firelib.finam.FinamDownloader
import firelib.model.tickers
import org.apache.commons.math3.stat.correlation.Covariance
import org.ojalgo.finance.portfolio.MarkowitzModel
import org.ojalgo.matrix.PrimitiveMatrix
import java.lang.Math.pow
import java.time.LocalDateTime
import java.util.*


data class InstrRec(val ticker : String,
                    val oneYearTarget : Double,
                    val threeYearTarget : Double,
                    val amount : Long
)

val writer = GeGeWriter(GlobalConstants.mdFolder.resolve("portfolio.db"), InstrRec::class, listOf("ticker"));


fun npv(rate : Double, vararg cacheFlows : Double) : Double{
    return cacheFlows.mapIndexed({idx, v->
        v/pow((1 + rate), (idx + 1).toDouble())
    }).sum()
}

val instr = finamMapperWriter().read()

fun updateFf(){
    finamMapperWriter().write(FinamDownloader().symbols())
}

val source = FinamDownloader(6000)

data class CacheEntry(val ticker : String,
                      val lastUpdated : Long,
                      val times : LongArray,
                      val returns : DoubleArray,
                      val close : Double

){
    fun toTreeMap() : TreeMap<Long,Double>{
        return TreeMap(times.mapIndexed({idx,time-> time to returns[idx]}).toMap())
    }
}

fun scaleMonthly(ret : Double) : Double{
    return pow((ret + 1), 12.0) - 1
}

val cacheLoader = GeGeWriter(GlobalConstants.mdFolder.resolve("cacheMonthly.db"), CacheEntry::class, listOf("ticker"))

val cache = cacheLoader.read().associateBy { it.ticker }.toMutableMap()

fun loadCached(ticker : String): CacheEntry {
    if(!cache.containsKey(ticker)){
        val instrId = instr.find { it.code == ticker && it.market == FinamDownloader.SHARES_MARKET }
        if(instrId == null){
            throw RuntimeException("no data for ${ticker}")
        }
        val load = source.load(instrId, LocalDateTime.now().minusDays(1400), Interval.Month).toList()
        val serie1 = load
        .map { it.endTime.atMoscow().toLocalDate().toEpochDay() to it.ret() }.toMap()
        val treeMap = TreeMap(serie1)
        val entry = CacheEntry(ticker, System.currentTimeMillis(), treeMap.keys.toLongArray(), treeMap.values.toDoubleArray(), load.last().close)

        cache[ticker] = entry

        cacheLoader.write(listOf(entry))
        return entry
    }
    return cache[ticker]!!
}


fun getCov(t0 : String, t1 : String) : Double{
    loadCached(t0)
    val map = loadCached(t0).toTreeMap()
    val map1 = loadCached(t1).toTreeMap()
    val fmap = map.filterKeys { map1.containsKey(it) }.mapValues { scaleMonthly(it.value) } .toSortedMap()
    val fmap1 = map1.filterKeys { map.containsKey(it) }.mapValues { scaleMonthly(it.value) }.toSortedMap()
    val covariance = Covariance()
    return covariance.covariance(fmap.values.toDoubleArray(), fmap1.values.toDoubleArray())
}

fun printMatrix(mx : PrimitiveMatrix){
    for(i in 0 until mx.countRows()){
        for(j in 0 until mx.countColumns()){
            print("${mx[i,j].toStrWithDecPlaces(3)} ")
        }
        println()
    }
}


fun main() {
    val tickers = tickers.map { it.toUpperCase() }
    val index = tickers.mapIndexed({ idx, ticker -> ticker to idx.toLong() }).toMap()
    val matrix = PrimitiveMatrix.FACTORY.makeDense(tickers.size, tickers.size)

    for(t0 in tickers){
        for(t1 in tickers){
            matrix.set(index[t0]!!, index[t1]!!, getCov(t0, t1))
        }
    }

    val matrixBuilt = matrix.build()

    val returns = PrimitiveMatrix.FACTORY.makeDense(tickers.size)

    val tickerToUpside = writer.read().map {
        val entry = loadCached(it.ticker)
        val upside = (npv(0.07, it.oneYearTarget, 0.0, it.threeYearTarget)/2 - entry.close) / entry.close
        it.ticker to upside
    }.toMap()


    tickerToUpside.forEach{ticker, ups->
        println("tick ${ticker}")
        println("${ticker} upside is ${ups} dispersion is ${matrixBuilt[index[ticker]!!,index[ticker]!!]}")
    }

    tickers.forEach {
        returns.set(index[it]!!,0L, tickerToUpside.getOrDefault(it, 0.0))
    }

    val rets = returns.build()

    val markowitzModel = MarkowitzModel(matrixBuilt, rets)


    markowitzModel.setTargetVariance(0.7.toBigDecimal())

    val weights = markowitzModel.weights

    tickers.forEachIndexed({idx,ticker->
        println("${ticker} to ${weights[idx].toDouble().toStrWithDecPlaces(3)}" )
    })
}

private fun printCorrelations(markowitzModel: MarkowitzModel) {

    for (i in 0 until tickers.size) {
        for (j in i until tickers.size) {
            val corr = markowitzModel.correlations[i.toLong(), j.toLong()]
            println("corr ${tickers[i]} ${tickers[j]} is $corr")
        }
    }
}

