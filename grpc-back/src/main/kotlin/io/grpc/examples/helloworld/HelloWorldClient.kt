/*
 * Copyright 2015 The gRPC Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.grpc.examples.helloworld

import com.firelib.StratServiceGrpc
import com.firelib.Tickers
import io.grpc.ManagedChannel
import io.grpc.ManagedChannelBuilder
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger

class HelloWorldClient
internal constructor(private val channel: ManagedChannel) {
    private val blockingStub: StratServiceGrpc.StratServiceBlockingStub
            = StratServiceGrpc.newBlockingStub(channel)

    constructor(host: String, port: Int) : this(ManagedChannelBuilder.forAddress(host, port)
            .usePlaintext()
            .build()) {
    }



    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    /** Say hello to server.  */
    fun greet(name: String) {
        logger.log(Level.INFO, "Will try to greet {0}...", name)
        blockingStub.subscribe(Tickers.newBuilder().build()).forEachRemaining({
            println("signal ${it}")
        })
    }

    companion object {
        private val logger = Logger.getLogger(HelloWorldClient::class.java.name)

        /**
         * Greet server. If provided, the first element of `args` is the name to use in the
         * greeting.
         */
        @Throws(Exception::class)
        @JvmStatic
        fun main(args: Array<String>) {
            val client = HelloWorldClient("localhost", 50051)
            try {
                /* Access a service running on the local machine on port 50051 */
                val user = if (args.size > 0) "${args[0]}" else "world"
                client.greet(user)
            } finally {
                client.shutdown()
            }
        }
    }
}
