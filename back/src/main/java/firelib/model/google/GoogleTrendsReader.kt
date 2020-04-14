package firelib.model.google

import firelib.core.GoogTrend
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import java.time.Instant

object GoogleTrendsReader{

    fun read(word : String) : List<GoogTrend>{
        val ret = mutableMapOf<Instant, GoogTrend>()
        val starts = mutableMapOf<Instant,Double>()

        val row = GeGeWriter(
            GlobalConstants.metaDb,
            GoogTrend::class,
            name = "google_trends_15d"
        ).read().filter {  it.word == word}.sortedBy { it.dt.toEpochMilli() + it.start.toEpochMilli()/10 }

        starts[row[0].start] = 1.0

        row.groupBy { it.start }.forEach{lst->
            if(ret.isEmpty()){
                lst.value.forEach { gt->
                    ret[gt.dt] = gt
                }
            }else{
                val scales = lst.value.flatMap {
                    if (ret.containsKey(it.dt) && ret[it.dt]!!.idx != 0L && it.idx != 0L) {
                        listOf(ret[it.dt]!!.idx.toDouble() / it.idx)
                    } else {
                        emptyList()
                    }
                }
                if(scales.any { it.isInfinite() }){
                    println()
                }
                println(scales)
                val avg = scales.average()

                lst.value.forEach { gt->
                    if(gt.idx != 0L){
                        ret[gt.dt] = gt.copy(idx = (gt.idx*avg).toLong())
                    }

                }

            }
        }
        return ret.values.sortedBy { it.dt }
    }
}

fun main() {
    GeGeWriter<GoogTrend>(GlobalConstants.metaDb, GoogTrend::class, name = "output_15d").write(
        GoogleTrendsReader.read(
            "sell stocks"
        )
    )
}