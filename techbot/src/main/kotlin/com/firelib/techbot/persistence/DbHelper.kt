package com.firelib.techbot.persistence

import com.firelib.techbot.*
import com.firelib.techbot.breachevent.BreachEventKey
import com.firelib.techbot.breachevent.BreachEvents
import com.firelib.techbot.domain.TimeFrame
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.staticdata.Instruments
import com.firelib.techbot.subscriptions.SourceSubscription
import com.firelib.techbot.tdline.SensitivityConfig
import com.firelib.techbot.usernotifier.NotifyGroup
import com.github.kotlintelegrambot.entities.ChatId
import com.github.kotlintelegrambot.entities.User
import firelib.core.misc.JsonHelper
import firelib.core.store.GlobalConstants
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory
import java.nio.file.Path
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

object DbHelper {

    /**
     * sinse sqlite can not be updated by concurrent threads
     */
    val updateExecutor: ExecutorService = Executors.newSingleThreadExecutor()

    val log = LoggerFactory.getLogger(javaClass)

    suspend fun initDefaultDb() {
        initDatabase(GlobalConstants.metaDb.toAbsolutePath())
    }

    suspend fun initDatabase(staticFile: Path) {
        log.info("initing database for path $staticFile")
        TransactionManager.defaultDatabase = Database.connect(
            "jdbc:sqlite:${staticFile}?journal_mode=WAL",
            driver = "org.sqlite.JDBC"
        )


        suspend fun populateSignalTypes() {
            updateDatabase("populate signal types") {
                val allSignals: Map<UserId, List<SignalType>> = getSignalTypes()
                Users.selectAll().forEach { userRow ->
                    val siggi = allSignals.getOrDefault(UserId(userRow[Users.userId]), emptyList())
                    if (siggi.isEmpty()) {
                        mainLogger.info("populating for user ${userRow[Users.name]}  ${userRow[Users.familyName]}")
                        SignalTypes.insert {
                            it[user] = userRow[Users.userId]
                            it[signalType] = SignalType.TREND_LINE.name
                        }
                        SignalTypes.insert {
                            it[user] = userRow[Users.userId]
                            it[signalType] = SignalType.DEMARK.name
                        }
                        SignalTypes.insert {
                            it[user] = userRow[Users.userId]
                            it[signalType] = SignalType.RSI_BOLINGER.name
                        }

                    }
                }
            }
        }



        transaction {

            try {
                exec("alter table breachevents drop column photo_file");
            } catch (e: Exception) {
                mainLogger.info("not dropping column probably already dropped")
            }


            addLogger(StdOutSqlLogger)
            SchemaUtils.create(Users)
            SchemaUtils.createMissingTablesAndColumns(Users)

            SchemaUtils.create(SensitivityConfig)
            SchemaUtils.createMissingTablesAndColumns(SensitivityConfig)

            SchemaUtils.create(BreachEvents)
            SchemaUtils.createMissingTablesAndColumns(BreachEvents)


            SchemaUtils.create(CommandsLog)
            SchemaUtils.createMissingTablesAndColumns(CommandsLog)

            SchemaUtils.create(TimeFrames)
            SchemaUtils.createMissingTablesAndColumns(TimeFrames)

            SchemaUtils.create(SignalTypes)
            SchemaUtils.createMissingTablesAndColumns(SignalTypes)

            SchemaUtils.create(Settings)
            SchemaUtils.createMissingTablesAndColumns(Settings)

            SchemaUtils.create(ConfigService)
            SchemaUtils.createMissingTablesAndColumns(ConfigService)

            SchemaUtils.create(Instruments)
            SchemaUtils.createMissingTablesAndColumns(Instruments)

            SchemaUtils.create(SourceSubscription)
            SchemaUtils.createMissingTablesAndColumns(SourceSubscription)

        }
        populateSignalTypes()

    }

    suspend fun <T> updateDatabase(name: String, runBlock: () -> T): T {

        fun blockInTransaction(): T {
            return transaction {
                try {
                    //addLogger(StdOutSqlLogger)
                    val (value, duration) = Misc.measureTime {
                        runBlock()
                    }
                    mainLogger.info("time spent on ${name} is ${duration / 1000.0} s.")
                    value
                } catch (e: Exception) {
                    Misc.dumpThreads()
                    mainLogger.error("exception in update database thread", e)
                    throw e
                }
            }
        }

        return suspendCoroutine<T> {
            if (Thread.currentThread().name == "dbExecutor") {
                mainLogger.info("executing ${name} without submitting")
                it.resumeWith(Result.success(runBlock()))
            } else {
                updateExecutor.submit {
                    try {
                        val result = Result.success(blockInTransaction())
                        it.resumeWith(result)
                    } catch (e: Exception) {
                        it.resumeWithException(e)
                    }
                }
            }
        }

    }

    fun getLatestBreachEvents(): List<BreachEventKey> {
        val sql =
            "select ticker, timeframe, event_type, max(event_time_ms) maxTime from breachevents where event_time_ms > ? group by ticker, timeframe, event_type "
        val ret = mutableListOf<BreachEventKey>()

        transaction {
            exec(sql, listOf(LongColumnType() to System.currentTimeMillis() - 10 * 24 * 3600_000L)) { rs ->
                while (rs.next()) {
                    ret.add(
                        BreachEventKey(
                            rs.getString("ticker"),
                            TimeFrame.valueOf(rs.getString("timeframe")),
                            rs.getLong("maxTime"),
                            SignalType.valueOf(rs.getString("event_Type"))
                        )
                    )
                }
            }

        }
        return ret
    }

    fun getNotifyGroups(): Map<NotifyGroup, List<UserId>> {
        val sql = """
                select s.user_id, s.source_id instrument_id, st.signalType, t.tf, se.settings  from sourcesubscription s, signaltypes st , timeframes t
                left join settings se on st.user_id = se.user_id and st.signalType = se.name
                where s.user_id = st.user_id and s.user_id = t.user_id ;              
        """.trimIndent()

        val ret = mutableListOf<Pair<NotifyGroup, UserId>>()

        transaction {
            exec(sql) { rs ->
                while (rs.next()) {
                    val instrId = rs.getString("instrument_id")
                    rs.getString("signalType")

                    val ss = rs.getString("settings")
                    ret.add(
                        NotifyGroup(
                            instrumentId = instrId,
                            signalType = SignalType.valueOf(rs.getString("signalType")),
                            timeFrame = TimeFrame.valueOf(rs.getString("tf")),
                            if (ss == null) emptyMap() else JsonHelper.fromJson(ss)
                        ) to UserId(rs.getString("user_id").toLong())
                    )
                }
            }

        }
        return ret.groupBy({ it.first }, { it.second })
    }

    fun getSignalTypes(): Map<UserId, List<SignalType>> {
        return transaction {
            SignalTypes.selectAll().map {
                UserId(it[SignalTypes.user].toLong()) to SignalType.valueOf(it[SignalTypes.signalType])
            }.groupBy({ it.first }, { it.second })
        }
    }

    fun readSettings(userId: Long): List<Map<String, String>> {
        return transaction {
            Settings.select { (Settings.user eq userId) }.map {
                JsonHelper.fromJson(it[Settings.value])
            }
        }
    }

    fun getTimeFrames(uid: ChatId): List<String> {
        return transaction {
            TimeFrames.select {
                TimeFrames.user eq uid.getId()
            }.withDistinct().map { it[TimeFrames.tf] }
        }
    }

    fun getSignalTypes(uid: ChatId): List<SignalType> {
        return transaction {
            SignalTypes.select {
                SignalTypes.user eq uid.getId()
            }.withDistinct().map { SignalType.valueOf(it[SignalTypes.signalType]) }
        }
    }

    suspend fun ensureExist(user: User) {
        updateDatabase("user update") {

            val llang = try {
                Langs.valueOf(user.languageCode!!).name
            } catch (e: Exception) {
                Langs.EN.name
            }


            if (Users.select { Users.userId eq user.id }.count() == 0L) {
                Users.insert {
                    it[userId] = user.id
                    it[name] = user.firstName
                    it[familyName] = user.lastName ?: "NA"
                    it[lang] = llang
                }
                TimeFrame.values().forEach { tff ->
                    TimeFrames.insert {
                        it[this.user] = user.id
                        it[tf] = tff.name
                    }
                }
                SignalType.values().filter { it != SignalType.MACD }.forEach { tff ->
                    SignalTypes.insert {
                        it[this.user] = user.id
                        it[this.signalType] = tff.name
                    }
                }
            }
        }
    }
}

suspend fun main() {
    DbHelper.initDatabase(GlobalConstants.metaDb.toAbsolutePath())

    DbHelper.getLatestBreachEvents().forEach {
        println(it)
    }
}