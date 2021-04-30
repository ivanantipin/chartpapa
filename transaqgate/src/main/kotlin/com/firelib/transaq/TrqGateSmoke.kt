package com.firelib.transaq

fun main() {

    System.setProperty("env", "prod")

    val stub = makeDefaultStub()

    val msgDispatcher = TrqMsgDispatcher(stub)

    val resp = msgDispatcher.stub.command(TrqCommandHelper.getPortfolio("519261BI6FUD"))
    println(resp)

    /*
    519261B6FU4
519261BI6FUD
     */

    //enableReconnect(msgDispatcher)

}
