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


val tickersUp = tickers.map { it.toUpperCase() }

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

fun updateInstruments(){
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

//fun descaleYearToMonth(annualUpside : Double) : Double{
//    if(annualUpside <= 0){
//        return 0.0
//    }
//    return Math.exp(Math.log(annualUpside + 1.0)/12.0) - 1.0
//}

val cacheLoader = GeGeWriter(GlobalConstants.mdFolder.resolve("cacheMonthly.db"), CacheEntry::class, listOf("ticker"))

val cache = cacheLoader.read().associateBy { it.ticker }.toMutableMap()

fun loadCached(ticker : String): CacheEntry {
    if(!cache.containsKey(ticker) || (System.currentTimeMillis() - cache[ticker]!!.lastUpdated)> 60_000*3600){
        val instrId = instr.find { it.code == ticker && it.market == FinamDownloader.SHARES_MARKET }
        if(instrId == null){
            throw RuntimeException("no data for ${ticker}")
        }
        val serie = source.load(instrId, LocalDateTime.now().minusDays(1400), Interval.Month).toList()
        val treeMap = TreeMap(serie.map { it.endTime.atMoscow().toLocalDate().toEpochDay() to it.ret() }.toMap())
        val entry = CacheEntry(ticker, System.currentTimeMillis(), treeMap.keys.toLongArray(), treeMap.values.toDoubleArray(), serie.last().close)
        cacheLoader.write(listOf(entry))
        cache[ticker] = entry
    }
    return cache[ticker]!!
}


fun getCov(t0 : String, t1 : String) : Double{
    val map = loadCached(t0).toTreeMap()
    val map1 = loadCached(t1).toTreeMap()
    val fmap = map.filterKeys { map1.containsKey(it) }.toSortedMap()
    val fmap1 = map1.filterKeys { map.containsKey(it) }.toSortedMap()
    val covariance = Covariance()
    return covariance.covariance(
        fmap.values.toDoubleArray(),
        fmap1.values.toDoubleArray())*12
}

fun printMatrix(mx : PrimitiveMatrix){
    for(i in 0 until mx.countRows()){
        for(j in 0 until mx.countColumns()){
            print("${mx[i,j].toStrWithDecPlaces(3)} ")
        }
    }
}



fun main() {
    val index = tickersUp.mapIndexed({ idx, ticker -> ticker to idx.toLong() }).toMap()
    val matrix = buildCovariance(index)

    val tickerToUpside = writer.read().map {
        val entry = loadCached(it.ticker)
        val upside = (npv(0.07, it.oneYearTarget, 0.0, it.threeYearTarget)/2 - entry.close) / entry.close
        it.ticker to  upside
    }.toMap()


    println("upsides dispersions")
    tickerToUpside.forEach{ticker, ups->
        println("${ticker}, ${ups.toStrWithDecPlaces(3)} , ${matrix[index[ticker]!!,index[ticker]!!].toDouble().toStrWithDecPlaces(5)}")
    }

    val markowitzModel = MarkowitzModel(matrix, getReturns(tickerToUpside))

    markowitzModel.setTargetReturn(0.20.toBigDecimal())
    markowitzModel.setLowerLimit(index["SBER"]!!.toInt(), 0.1.toBigDecimal())

    val weights = markowitzModel.weights

    println(weights.size)

    println("==== weights ====")
    tickersUp.forEachIndexed({idx,ticker->
        if(weights[idx] > 0.000001.toBigDecimal()){
            println("${ticker} to ${weights[idx].toDouble().toStrWithDecPlaces(3)} corr with sber ${markowitzModel.correlations[idx.toLong(), index["SBER"]!!.toLong()]}" )
        }
    })
}

private fun buildCovariance(index: Map<String, Long>): PrimitiveMatrix {
    val matrix = PrimitiveMatrix.FACTORY.makeDense(tickers.size, tickers.size)
    for (t0 in tickersUp) {
        for (t1 in tickersUp) {
            matrix.set(index[t0]!!, index[t1]!!, getCov(t0, t1))
        }
    }
    return matrix.build()
}

private fun getReturns(
    tickerToUpside: Map<String, Double>
): PrimitiveMatrix? {
    val returns = PrimitiveMatrix.FACTORY.makeDense(tickers.size)
    tickersUp.forEachIndexed {idx, ticker->
        returns.set(idx.toLong(), 0L, tickerToUpside.getOrDefault(ticker, 0.01))
    }
    return returns.build()
}

private fun printCorrelations(markowitzModel: MarkowitzModel) {

    for (i in 0 until tickersUp.size) {
        for (j in i until tickersUp.size) {
            val corr = markowitzModel.correlations[i.toLong(), j.toLong()]
            println("corr ${tickersUp[i]} ${tickersUp[j]} is $corr")
        }
    }
}

