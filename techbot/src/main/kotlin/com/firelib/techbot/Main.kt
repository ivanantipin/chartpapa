package com.firelib.techbot

import com.firelib.techbot.UpdateSensitivities.updateSensitivties
import com.firelib.techbot.persistence.ConfigService
import firelib.core.store.GlobalConstants

fun main() {
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
        mainLogger.error("unhandled exception thrown", throwable)
    }
    DbIniter.initDatabase(GlobalConstants.metaDb.toAbsolutePath())
    ConfigService.initSystemVars()
    val app = TechBotApp()
    app.start()
    updateSensitivties(app.getSubscriptionService(), app.ohlcService())
}



