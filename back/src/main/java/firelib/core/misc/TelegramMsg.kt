package firelib.core.misc

import firelib.core.store.GlobalConstants
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate

object TelegramMsg {

    val log = LoggerFactory.getLogger(javaClass)

    val telegramToken by lazy{
        GlobalConstants.getProp("telegram.bot.token")
    }
    val telegramChannel by lazy{
        GlobalConstants.getProp("telegram.channel")
    }
    fun sendMsg(msg : String){
        try {
            val template = RestTemplate()
            template.getForEntity(
                "https://api.telegram.org/${telegramToken}/sendMessage?chat_id=@${telegramChannel}&text=${msg}",
                String::class.java
            )
        }catch (e : Exception){
            log.error("error sending message ${msg}", e)
        }
    }
}
