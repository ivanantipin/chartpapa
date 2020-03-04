package com.firelib.transaq

import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit

fun enableReconnect(trqMsgDispatcher : TrqMsgDispatcher) {
    val callbacker = trqMsgDispatcher.add<ServerStatus> { it is ServerStatus }
    val log = LoggerFactory.getLogger("reconnector")

    Thread{
        while (true) {
            try {
                trqMsgDispatcher.stub.command(TrqCommandHelper.statusCmd())
                Thread.sleep(5*60_000)
            }catch (e : Exception){
                log.error("failed to send status command", e)
                Thread.sleep(60_000)
            }
        }
    }.start()

    Thread {
        while (true) {
            try {
                val status = callbacker.queue.poll(1, TimeUnit.MINUTES)
                if(status != null){
                    log.info("current status is ${status}")
                }
                if ("true" != status?.connected) {
                    trqMsgDispatcher.stub.command(loginCmd)
                    Thread.sleep(60000)
                }
            }catch (e : Exception){
                log.error("failed to process reconnect loop, probably due to network problem", e)
                Thread.sleep(60_000)
            }
        }
    }.apply { isDaemon = true }.start()

    trqMsgDispatcher.stub.command(loginCmd)
}

val msgLogger = LoggerFactory.getLogger("msglogger")

fun enableMsgLogging(trqMsgDispatcher: TrqMsgDispatcher){
    trqMsgDispatcher.addSync<TrqMsg>({ true }, {
        msgLogger.info("got transaq message ${it}")
    })
}