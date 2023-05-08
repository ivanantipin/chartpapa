package com.firelib.techbot

import com.firelib.techbot.persistence.ConfigService
import com.firelib.techbot.persistence.DbHelper
import com.firelib.techbot.tdline.UpdateTrendLinesSensitivities.updateSensitivities
import firelib.core.store.GlobalConstants
import org.slf4j.LoggerFactory

val mainLogger = LoggerFactory.getLogger("main")

suspend fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }
    InflightHandler.start()
    DbHelper.initDefaultDb()
    ConfigService.initSystemVars()
    val app = TechbotApp()
    app.start()
    updateSensitivities(app.subscriptionService(), app.ohlcService())
}

