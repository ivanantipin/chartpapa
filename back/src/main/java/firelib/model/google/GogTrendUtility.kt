package firelib.model.google

import firelib.core.GoogTrend
import firelib.core.GoogTrendMulti
import firelib.core.report.dao.GeGeWriter
import firelib.core.store.GlobalConstants
import java.time.Instant

object GogTrendUtility{

    fun getTrends(words : Array<String>) : List<GoogTrendMulti>{

        val rmap = mutableMapOf<Instant, MutableMap<String,Long>>()

        GeGeWriter(
            GlobalConstants.metaDb,
            GoogTrend::class,
            name = "google_trends"
        ).read().sortedBy { it.dt }
            .filter { words.contains(it.word) }.forEach {
                val mp = rmap.computeIfAbsent(it.dt, { mutableMapOf<String, Long>() })
                mp.put(it.word, it.idx)
            }

        return rmap.map { GoogTrendMulti(it.key, it.value) }.sortedBy { it.dt }

    }

    fun getMaDiff(word : Array<String>, period: Int, funk : (GoogTrendMulti)->Double ) : GoogMaDiff {
        return GoogMaDiff(
            getTrends(
                word
            ).map({ Pair(it.dt, funk(it)) }), period
        )
    }

}