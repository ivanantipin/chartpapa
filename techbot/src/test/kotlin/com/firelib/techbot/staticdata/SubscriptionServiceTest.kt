package com.firelib.techbot.staticdata

import com.firelib.techbot.domain.UserId
import com.firelib.techbot.persistence.ConfigService
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.subscriptions.SubscriptionService
import firelib.core.SourceName
import firelib.core.domain.InstrId
import kotlinx.coroutines.runBlocking
import org.junit.Assert
import org.junit.Test
import java.nio.file.Paths

class SubscriptionServiceTest {

    @Test
    fun testSubService() {

        runBlocking {
            DbHelper.initDatabase(Paths.get("/tmp/${System.currentTimeMillis()}.db"))
            ConfigService.initSystemVars()

            val instrIdDao = InstrIdDao()

            val instrId = InstrId("id", market = "market", source = SourceName.DUMMY.name, code = "code")
            val instrId1 = InstrId("id1", market = "market", source = SourceName.DUMMY.name, code = "code")

            instrIdDao.replaceSourceInstruments(
                listOf(
                    instrId,
                    instrId1
                )
            )
            instrIdDao.replaceSourceInstruments(
                listOf(
                    instrId,
                    instrId1
                )
            )

            Assert.assertEquals(2, instrIdDao.loadAll().size)

            val staticDataService = InstrumentsService(instrIdDao)

            val subService = SubscriptionService(staticDataService)

            val userId = UserId(0)
            subService.addSubscription(userId, instrId)
            subService.addSubscription(userId, instrId1)


            Assert.assertEquals(1, SubscriptionService(staticDataService).subscriptions.size)
            Assert.assertEquals(2, SubscriptionService(staticDataService).subscriptions[userId]!!.size)
        }



    }
}