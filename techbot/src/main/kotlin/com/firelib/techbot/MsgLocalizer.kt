package com.firelib.techbot

enum class Langs {
    RU, EN
}

enum class MsgLocalizer {
    TECH_ANALYSIS,
    MAIN_MENU,
    SETTINGS,
    HELP,
    HOME,
    FUNDAMENTALS,
    MacdConf,
    Instruments,
    AddSymbol,
    YourSymbolsOrRemoval,
    YourSymbolsPressToRemove,
    SettingsU,
    Unsubscribe,
    Language,
    AddTf,
    TfsTitle,
    UnsubscribeFromSignal,
    SupportChannel,
    SupportMsg,
    RsiBolingerConf,
    AddSignalType,
    OtherSettings,
    ChooseTfFor,

    TREND_LINE,
    DEMARK,
    MACD,
    RSI_BOLINGER,
    TDST,
    Companies,
    PressTfToUnsubscribe,
    PressSignalToSubscribe,
    YourSignalsOrRemoval,
    YourTimeframes,
    Choose1stLetterOfCompany,
    ChooseCompanyToSubscribe,
    PickCompany,
    SubscrptionRemoved,
    TimeFrameAdded,
    ChooseLanguage,

    ;

    fun toLocal(langCode: Langs): String {
        return getMsg(langCode, this)
    }

    companion object {
        private var reverseMap: Map<String, MsgLocalizer>

        val map = mapOf(
            Langs.RU to mapOf<MsgLocalizer, String>(
                TECH_ANALYSIS to "Технический Анализ",
                MAIN_MENU to "Главное меню",
                SETTINGS to "Настройки",
                HELP to "Помощь",
                MacdConf to "MACD Конфигурация",
                FUNDAMENTALS to "Фундаментальные данные",
                Instruments to "Мои инструменты",
                AddSymbol to "Добавить символ",
                YourSymbolsOrRemoval to "Ваши символы / Удаление",
                SettingsU to "Установки",
                Unsubscribe to "Отписаться от таймфрейма",
                AddTf to "Добавить таймфрейм",
                UnsubscribeFromSignal to "Отписаться от сигнала",
                SupportChannel to "Канал поддержки",
                RsiBolingerConf to "RSI-BOLINGER Конфигурация",
                AddSignalType to "Добавить тип сигнала",
                OtherSettings to "Другие настройки",
                ChooseTfFor to "Выберите таймфрейм для ",
                PressTfToUnsubscribe to "Нажмите на таймфрейм чтобы отписаться",
                TfsTitle to "Нажмите на таймфрейм чтобы подписаться",
                YourSignalsOrRemoval to "==Ваши подписки==\nнажмите чтобы отписаться",
                YourTimeframes to "*Ваши таймфреймы*\n",
                Choose1stLetterOfCompany to "Выберите первую букву тикера вы хотиде добавить",
                PickCompany to "Выберите компанию",
                SubscrptionRemoved to "Подписка удалена для ",
                TimeFrameAdded to "Таймфрейм добавлен ",
                TREND_LINE to "Трендовые линии",
                DEMARK to "DeMark Секвента",
                TDST to "Горизонтальные уровни",
                RSI_BOLINGER to "RSI-Bolinger Система",
                MACD to "MACD",
                SupportMsg to "Поддержка",
                YourSymbolsPressToRemove to "*Ваши инструменты*\nНажмите чтобы отписаться"
            ),

            Langs.EN to mapOf<MsgLocalizer, String>(
                TECH_ANALYSIS to "Technical Analysis",
                MAIN_MENU to "Main Menu",
                SETTINGS to "Settings",
                HELP to "Help",
                MacdConf to "Macd configuration",
                FUNDAMENTALS to "Fundamentals",
                Instruments to "My instruments",
                AddSymbol to "Add Instrument",
                YourSymbolsOrRemoval to "My instruments / Removal",
                SettingsU to "Settings",
                Unsubscribe to "Unsubscribe from timeframe",
                AddTf to "Add timeframe",
                UnsubscribeFromSignal to "Unsubscribe from signal",
                SupportChannel to "Support channel",
                RsiBolingerConf to "RSI-BOLINGER congfigartion",
                AddSignalType to "Add signal",
                OtherSettings to "Other settings",
                ChooseTfFor to "Choose timeframe for ",
                PressTfToUnsubscribe to "Press timeframe to unsubscribe",
                TfsTitle to "Press timeframe to subscribe",
                YourSignalsOrRemoval to "*Your signals*\npress to unsubscribe",
                PressSignalToSubscribe to "Press to subscribe for a signal",
                YourTimeframes to "*Your timeframes*\n",
                Choose1stLetterOfCompany to "Choose 1st letter of ticker you want to subscribe to",
                PickCompany to "Choose company",
                SubscrptionRemoved to "Subscription removed for ",
                TimeFrameAdded to "Timeframe added ",
                TREND_LINE to "Trend Lines",
                DEMARK to "DeMark Sequenta",
                TDST to "Horizontal Levels",
                RSI_BOLINGER to "Rsi-Bolinger System",
                MACD to "MACD",
                SupportMsg to "Support",
                YourSymbolsPressToRemove to "*Your instruments*\nPress to remove"
            )
        )

        init {
            val entries = map.values.flatMap { it.entries }

            this.reverseMap = (entries.associateBy({ it.value }, { it.key }) + MsgLocalizer.values().associateBy { it.name })
        }

        fun getMsg(lang: Langs, msgLocalizer: MsgLocalizer): String {
            return map[lang]!!.getOrDefault(msgLocalizer, msgLocalizer.name)
        }

        fun getReverseMap(msg: String): MsgLocalizer? {
            return reverseMap.get(msg)
        }

    }
}


