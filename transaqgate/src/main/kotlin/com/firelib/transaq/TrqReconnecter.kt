package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.util.concurrent.TimeUnit

fun enableReconnect(stub: TransaqConnectorGrpc.TransaqConnectorBlockingStub) {
    val callbacker = MsgCallbacker(stub).add<ServerStatus> { it is ServerStatus }
    val log = LoggerFactory.getLogger("reconnector")
    Thread {
        while (true) {
            try {
                val status = callbacker.queue.poll(1, TimeUnit.MINUTES)
                if ("true" != status?.connected) {
                    println("current status is ${status}")
                    stub.command(loginCmd)
                    Thread.sleep(10000)
                }
            }catch (e : Exception){
                log.error("failed to process reconnect loop, probably due to network problem", e)
            }


        }
    }.apply { isDaemon = true }.start()
    stub.command(loginCmd)
}

fun main() {
   makeDefaultStub().command(loginCmd)
}