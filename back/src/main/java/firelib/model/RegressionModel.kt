package firelib.model

import firelib.core.SourceName
import firelib.core.config.ModelBacktestConfig
import firelib.core.config.runStrat
import firelib.core.domain.Interval
import firelib.core.domain.Ohlc
import firelib.core.misc.Quantiles
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.DbReaderFactory
import firelib.core.timeseries.TimeSeries
import firelib.core.timeseries.ret
import org.apache.commons.math3.stat.regression.OLSMultipleLinearRegression
import java.time.LocalDate
import java.util.*


data class StatDd(val ri : Double, val sp500 : Double, val brent : Double, val regres : Double)
data class RegParam(val a : Double, val b : Double)

class RegressionModel(context: ModelContext, val props: Map<String, String>) : Model(context,props){

    val period = props["period"]!!.toInt()

    val lst = mutableListOf<StatDd>()
    val lst1 = mutableListOf<RegParam>()

    val ylist = LinkedList<Double>()
    val xlist = LinkedList<DoubleArray>()

    init {
        val tss = enableSeries(Interval.Min240,1000, true)
        val reg = OLSMultipleLinearRegression()
        reg.isNoIntercept = true



        val qq = Quantiles<Double>(100)

        tss[0].preRollSubscribe {
            if(it.count() > 100){


                if(notInterpolatedLast(tss,0)){

                    ylist += tss[0].ret(100)
                    val arr = DoubleArray(2)
                    arr[0] = tss[1].ret(100)
                    arr[1] = tss[2].ret(100)
                    xlist += arr
                    if(ylist.size > 500){
                        ylist.poll()
                        xlist.poll()
                    }

                    if(ylist.size == 500){
                        reg.newSampleData(ylist.toDoubleArray(), xlist.toTypedArray())

                        val ri = tss[0].ret(100)
                        val brent = tss[1].ret(100)
                        val sp500 = tss[2].ret(100)

                        val params = reg.estimateRegressionParameters()

                        val est = params[0] * brent + params[1] * sp500

                        val res = reg.estimateResiduals().last()

                        println(" residual ${reg.estimateResiduals().last()} vs ${est  - ri}")


                        if(est.isFinite()){
                            lst1 += RegParam(params[0],params[1])
                            lst += StatDd(ri = ri, sp500 = sp500, brent = brent, regres = est)
                        }

                        enableFactor("r2",{
                            val ret = reg.calculateRSquared()
                            if(ret.isFinite()) ret else 0.0
                        })

                        val mm = qq.getQuantile(res)
                        if(mm > 0.95){
                            shortForMoneyIfFlat(0, 1000_000)
                        } else if(mm < 0.05){
                            longForMoneyIfFlat(0, 1000_000)
                        } else if(mm > 0.1 && mm < 0.9){
                            flattenAll(0)
                        }
                        qq.add(res)
                    }

                }

            }
        }
    }

    override fun onBacktestEnd() {
        val writer = GeGeWriter(context.config.getReportDbFile(), StatDd::class)
        writer.write(lst)

        val ppw = GeGeWriter(context.config.getReportDbFile(), RegParam::class)
        ppw.write(lst1)


    }

    private fun notInterpolatedLast(tss: List<TimeSeries<Ohlc>>, idx : Int) =
        !tss[0][idx].interpolated && !tss[1][idx].interpolated && !tss[2][idx].interpolated
}

fun regressModel(): ModelBacktestConfig {
    return ModelBacktestConfig(RegressionModel::class).apply{
        startDate(LocalDate.now().minusDays(3000))
        //opt("period", 10,500, 5)
        param("period", 10)
        interval = Interval.Min240
        backtestReaderFactory = DbReaderFactory(
            SourceName.MT5,
            Interval.Min240,
            roundedStartTime()
        )

        instruments = listOf("ALLFUTRTSI","ALLFUTBRENT","FUTSP500CONT")
    }
}

fun main() {
    regressModel().runStrat()
}