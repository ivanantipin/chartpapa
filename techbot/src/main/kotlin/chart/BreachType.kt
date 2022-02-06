package chart

import com.firelib.techbot.command.MacdCommand
import com.firelib.techbot.command.RsiBolingerCommand
import com.github.kotlintelegrambot.Bot
import com.github.kotlintelegrambot.entities.Update

enum class SignalType(
    val settingsName : String,
    val parsePayload : (List<String>)->Map<String,String> = {emptyMap()},
    val validate : (List<String>)->Boolean = {true},
    val displayHelp : (bot: Bot, update: Update)->Unit
){
    TREND_LINE("trend", displayHelp = {b, u->"Установки не предусмотрены}"}),
    DEMARK("demark",  displayHelp = {b, u->"Установки не предусмотрены}"}),
    MACD("macd", MacdCommand::parsePayload,  displayHelp = MacdCommand::displayMACD_Help, validate = MacdCommand::validate),
    RSI_BOLINGER("rbc", RsiBolingerCommand::parsePayload, displayHelp = RsiBolingerCommand::displayHelp, validate = RsiBolingerCommand::validate)

}

enum class BreachType {
    TREND_LINE, DEMARK_SIGNAL, TREND_LINE_SNAPSHOT, LEVELS_SNAPSHOT, LEVELS_SIGNAL, MACD, RSI_BOLINGER
}