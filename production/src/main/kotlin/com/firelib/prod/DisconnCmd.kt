package com.firelib.prod

import com.firelib.transaq.command
import com.firelib.transaq.loginCmd
import com.firelib.transaq.makeDefaultStub

fun main() {
    makeDefaultStub().command(loginCmd)
}