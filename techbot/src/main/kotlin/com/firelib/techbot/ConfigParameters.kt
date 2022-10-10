package com.firelib.techbot

enum class ConfigParameters {
    TELEGRAM_TOKEN, POLYGON_TOKEN, NOTIFICATIONS_ENABLED;

    fun get(): String? {
        return System.getProperty(this.name)
    }
}