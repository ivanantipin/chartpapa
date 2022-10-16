package com.firelib.techbot

import com.firelib.techbot.tdline.UpdateTrendLinesSensitivities.updateSensitivities
import com.firelib.techbot.persistence.ConfigService
import com.firelib.techbot.persistence.DbHelper
import firelib.core.store.GlobalConstants
import org.slf4j.LoggerFactory

val mainLogger = LoggerFactory.getLogger("main")

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }
    DbHelper.initDatabase(GlobalConstants.metaDb.toAbsolutePath())
    ConfigService.initSystemVars()
    val app = TechbotApp()
    app.start()
    updateSensitivities(app.subscriptionService(), app.ohlcService())
}