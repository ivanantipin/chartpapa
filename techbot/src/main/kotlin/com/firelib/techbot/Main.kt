package com.firelib.techbot

import com.firelib.techbot.tdline.UpdateTrendLinesSensitivities.updateSensitivities
import com.firelib.techbot.persistence.ConfigService
import com.firelib.techbot.persistence.DbIniter
import firelib.core.store.GlobalConstants

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }
    DbIniter.initDatabase(GlobalConstants.metaDb.toAbsolutePath())
    ConfigService.initSystemVars()
    val app = TechBotApp()
    app.start()
    updateSensitivities(app.subscriptionService(), app.ohlcService())
}



