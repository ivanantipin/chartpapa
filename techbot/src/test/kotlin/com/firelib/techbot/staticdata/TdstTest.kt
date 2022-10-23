package com.firelib.techbot.staticdata

import com.firelib.techbot.BotInterface
import com.firelib.techbot.TechbotApp
import com.firelib.techbot.command.Cmd
import com.firelib.techbot.domain.UserId
import com.firelib.techbot.persistence.DbHelper
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Message
import com.github.kotlintelegrambot.entities.Update
import com.github.kotlintelegrambot.entities.User
import firelib.core.HistoricalSource
import firelib.core.SourceName
import firelib.core.domain.InstrId
import firelib.core.domain.Interval
import firelib.core.store.GlobalConstants
import firelib.core.store.HistoricalSourceProvider
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.Test
import java.nio.file.Paths

class TdstTest {



    @Test
    fun testSignals(){

        DbHelper.initDatabase(Paths.get("/tmp/test${System.currentTimeMillis()}.db"))

        val botInterface = mockk<BotInterface>(relaxed = true)
        val sourceProvider = mockk<HistoricalSourceProvider>()

        val bot = mockk<Bot>(relaxed = true)

        val instrIdDao = InstrIdDao()

        val instrId = InstrId(id = "KLXE_XNAS", code = "KLXE", market = "XNAS", source = SourceName.POLIGON.name)

        instrIdDao.addAll(listOf(instrId))

        class MockedApp : TechbotApp(){
            override fun botInterface(): BotInterface {
                return botInterface
            }

            override fun historicalSourceProvider(): HistoricalSourceProvider {
                return sourceProvider
            }
        }

        val mockedApp = MockedApp()

        mockedApp.subscribeHandler().handle(Cmd("", mapOf("id" to instrId.id)), bot, User(0L, firstName = "Ivan", lastName = "Antipin", isBot = false))

        val poligon = mockk<HistoricalSource>()

        every { poligon.load(instrId, any(), Interval.Min10) } returns emptySequence()

        every { sourceProvider.get(SourceName.POLIGON) } returns poligon

        mockedApp.getUserNotifier().check(2)

 //       verify(exactly = 1) {  botInterface.sendBreachEvent(any(), any(), listOf(UserId(0))) }


    }

}