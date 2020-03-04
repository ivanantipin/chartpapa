package com.firelib.prod

import com.firelib.transaq.TrqMsgDispatcher
import com.firelib.transaq.enableMsgLogging
import com.firelib.transaq.makeDefaultStub

fun main() {
    enableMsgLogging(TrqMsgDispatcher(makeDefaultStub()))
}