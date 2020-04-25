package firelib.core.misc

import firelib.core.store.GlobalConstants
import org.slf4j.LoggerFactory
import org.springframework.web.client.RestTemplate

object TelegramMsg {

    val log = LoggerFactory.getLogger(javaClass)

    val telegramToken by lazy{
        GlobalConstants.getProp("telegram.bot.token")
    }
    fun sendMsg(msg : String){
        try {
            val template = RestTemplate()
            template.getForEntity(
                "https://api.telegram.org/${telegramToken}/sendMessage?chat_id=@iaasignals&text=${msg}",
                String::class.java
            )
        }catch (e : Exception){
            log.error("error sending message ${msg}")
        }
    }
}
