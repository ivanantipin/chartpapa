package com.funstat.store


import com.funstat.GlobalConstants
import com.funstat.Pair
import com.funstat.Tables
import com.funstat.domain.InstrId
import com.funstat.finam.FinamDownloader
import com.funstat.iqfeed.IntervalTransformer
import com.funstat.iqfeed.IqFeedSource
import com.funstat.vantage.Source
import com.funstat.vantage.VSymbolDownloader
import com.funstat.vantage.VantageDownloader
import firelib.common.interval.Interval
import firelib.common.misc.atUtc
import firelib.common.model.DivHelper
import firelib.domain.Ohlc
import org.apache.commons.io.FileUtils
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import java.io.File
import java.io.IOException
import java.nio.file.Paths
import java.time.LocalDateTime
import java.time.ZoneOffset
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit


class MdStorageImpl(private val folder: String = GlobalConstants.mdFolder.toString()) : MdStorage {

    internal var container = SingletonsContainer()

    internal var sources: Map<String, Source> = object : HashMap<String, Source>() {
        init {
            put(FinamDownloader.SOURCE, FinamDownloader())
            put(VantageDownloader.SOURCE, VantageDownloader());
            put(IqFeedSource.SOURCE, IqFeedSource(Paths.get("/ddisk/globaldatabase/1MIN/STK")))
        }
    }

    private val executor = Executors.newScheduledThreadPool(1)

    internal val generic: GenericDao
        get() = container.get<GenericDaoImpl>("generic dao") { GenericDaoImpl(SqlUtils.getDsForFile("$folder/meta.db")) }

    init {
        try {
            FileUtils.forceMkdir(File(folder))
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

    }

    fun getDao(source: String, interval: String): MdDao {
        return container.get("$source/$interval") {
            val folder = this.folder + "/" + source + "/"
            try {
                FileUtils.forceMkdir(File(folder))
            } catch (e: IOException) {
                throw RuntimeException(e)
            }

            val ds = SqlUtils.getDsForFile("$folder$interval.db")
            MdDao(ds)
        }
    }


    override fun read(instrId: InstrId, interval: String): List<Ohlc> {
        Tables.REQUESTED.writeSingle(generic, instrId)
        val dao = getDao(instrId.source, sources[instrId.source]!!.getDefaultInterval().name)
        val target = Interval.valueOf(interval)
        val startTime = LocalDateTime.now().minusSeconds(target.durationMs * 600 / 1000)
        var ret = dao.queryAll(instrId.code,startTime)

        if (ret.isEmpty()) {
            updateMarketData(instrId)
            ret = dao.queryAll(instrId.code,startTime)
        }
        val start = System.currentTimeMillis()
        try {
            return IntervalTransformer.transform(target, ret)
        } finally {
            println("transformed in " + (System.currentTimeMillis() - start) / 1000.0 + " s. " + ret.size + " min bars")
        }
    }

    override fun save(code: String, source: String, interval: String, data: List<firelib.domain.Ohlc>) {
        getDao(source, interval).insertOhlc(data, code)
    }

    override fun start() {
        executor.scheduleAtFixedRate({
            try {
                Tables.REQUESTED.read(generic).forEach { symbol -> updateMarketData(symbol) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, 0, 10, TimeUnit.MINUTES)
    }

    override fun updateSymbolsMeta() {
        val lastUpdated = Tables.PAIRS.readByKey(generic, SYMBOLS_LAST_UPDATED)
        if (lastUpdated == null || System.currentTimeMillis() - java.lang.Long.parseLong(lastUpdated.value) > 24 * 3600000) {
            println("updating symbols as they are stale")
            Tables.SYMBOLS.write(generic, sources.values.flatMap {  s -> s.symbols() }.filter { s -> s.market == "1"
                    || s.source == VantageDownloader.SOURCE
                    || s.source == IqFeedSource.SOURCE })
            Tables.PAIRS.writeSingle(generic, Pair(SYMBOLS_LAST_UPDATED, "" + System.currentTimeMillis()))
        } else {
            println("not updating symbols as last update was " + LocalDateTime.ofEpochSecond(java.lang.Long.parseLong(lastUpdated.value) / 1000, 0, ZoneOffset.UTC))
        }

        if (false) {
            //fixme
            updateVantage()
        }

    }

    private fun updateVantage() {
        val lastVantageUpdated = Tables.PAIRS.readByKey(generic, VANTAGE_LAST_UPDATED)
        if (lastVantageUpdated == null) {
            val exec = Executors.newSingleThreadExecutor()
            exec.submit {
                VSymbolDownloader.updateVantageSymbols(generic)
                Tables.PAIRS.writeSingle(generic, Pair(VANTAGE_LAST_UPDATED, "" + System.currentTimeMillis()))
            }
        }
    }


    override fun meta(): List<InstrId> {
        return container.get(SYMBOLS_TABLE) { Tables.SYMBOLS.read(generic) }
    }


    fun updateMarketData(instrId: InstrId) {
        println("updating ${instrId}")
        try{
            val source = sources[instrId.source]!!
            val dao = getDao(instrId.source, source.getDefaultInterval().name)
            val startTime =  dao.queryLast(instrId.code).map { oh -> oh.dtGmtEnd.atUtc().minusDays(2) }.orElse(LocalDateTime.now().minusDays(3000))

            source.load(instrId, startTime).chunked(5000).forEach {
                dao.insertOhlc(it, instrId.code)
            }

        }catch (e : Exception){
            println("failed to update "+ instrId + " " + e.message)
            e.printStackTrace()
        }
    }

    companion object {

        val HOME_PATH = "/ddisk/globaldatabase/md"


        val SYMBOLS_TABLE = "symbols"
        val SYMBOLS_LAST_UPDATED = "SYMBOLS_LAST_UPDATED"
        private val VANTAGE_LAST_UPDATED = "VANTAGE_LAST_UPDATED"


        @JvmStatic
        fun main(args: Array<String>) {
            val mdStorage = MdStorageImpl(HOME_PATH)
            mdStorage.updateSymbolsMeta()
            println(mdStorage.meta().filter { s -> s.source == FinamDownloader.SOURCE })

        }
    }

}


object SqlUtils{
    fun getDsForFile(file: String): SQLiteDataSource {

        val sqLiteConfig = SQLiteConfig()
        //sqLiteConfig.setPragma(Pragma.DATE_STRING_FORMAT, "yyyy-MM-dd HH:mm:ss")

        val ds = SQLiteDataSource(sqLiteConfig)
        ds.url = "jdbc:sqlite:$file"
        return ds
    }
}

fun main(args: Array<String>) {

    //MdStorageImpl().updateMarketData(FinamDownloader().symbols().find { it.code == "SBER" && it.market == "1" }!!)

    DivHelper.getDivs().get("phor")!!.forEach {  println(it)}


}