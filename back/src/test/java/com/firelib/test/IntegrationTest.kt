package com.firelib.test

//
//class OhlcTestModel(context: ModelContext) : Model(context, emptyMap()) {
//
//    val startTimesGmt = ArrayList<Instant>();
//
//
//    private var hist: TimeSeries<Ohlc>
//
//
//    var dayHist: TimeSeries<Ohlc>
//
//
//    val uniqTimes = HashSet<Instant>()
//
//
//    val bars = ArrayList<Ohlc>()
//
//
//    init {
//        testHelper.instanceOhlc = this
//        hist = context.mdDistributor.getOrCreateTs(0, Interval.Min5, 10)
//        hist.preRollSubscribe { on5Min(it) }
//        dayHist = context.mdDistributor.getOrCreateTs(0, Interval.Day, 10)
//    }
//
//
//    fun on5Min(ts: TimeSeries<Ohlc>) {
//        if (dayHist.count() > 0 && dayHist[0].endTime.truncatedTo(ChronoUnit.DAYS) != dayHist[0].endTime) {
//            throw Exception("time of day ts not correct");
//        }
//        val currentTime = context.timeService.currentTime()
//        if (currentTime != ts[0].endTime) {
//            throw Exception("time is not equal $currentTime <> ${ts[0].endTime}");
//        }
//        bars += ts[0].copy()
//
//        if (bars.size > 1) {
//            if ((ts[0].endTime.toEpochMilli() - ts[1].endTime.toEpochMilli()) != 5 * 60 * 1000L) {
//                throw Exception("not 5 min diff " + ts[0].endTime + " -- " + ts[1].endTime);
//            }
//        }
//        addOhlc(ts[0]);
//    }
//
//    fun addOhlc(pQuote: Ohlc) {
//        if (uniqTimes.contains(pQuote.endTime)) {
//            throw Exception("dupe time " + pQuote.endTime);
//        }
//        uniqTimes.add(pQuote.endTime)
//
//        if (startTimesGmt.size == 0 || startTimesGmt.last().truncatedTo(ChronoUnit.DAYS) != pQuote.endTime.truncatedTo(
//                ChronoUnit.DAYS)) {
//            startTimesGmt += pQuote.endTime
//        }
//    }
//
//    override fun onBacktestEnd(): Unit {}
//}
//
//
//class BacktestIntegrationTest {
//
//    fun genInterval(start : LocalDate, end : LocalDate) : List<Ohlc>{
//        return emptyList()
//    }
//
//    @Test
//    fun test(){
//
//        val cfg = ModelBacktestConfig(OhlcTestModel::class).apply {
//            interval = Interval.Min10
//            startDate(LocalDate.now().minusDays(3000))
//            instruments = listOf("sber","sberp")
//            backtestReaderFactory = object : ReaderFactory{
//                override fun makeReader(security: String): SimplifiedReader {
//                    return QueueSimplifiedReader()
//                }
//
//            }
//        }
//
//
//        val conf = CandleMax.modelConfig()
//        println(conf.instruments)
//
//        conf.runStrat()
//
//
//    }
//}