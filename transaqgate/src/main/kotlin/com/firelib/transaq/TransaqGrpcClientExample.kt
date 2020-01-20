package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


class TransaqGrpcClientExample  {
    var channel: io.grpc.ManagedChannel
    val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub


    private fun buildSslContext(trustCertCollectionFilePath: String,
                                clientCertChainFilePath: String,
                                clientPrivateKeyFilePath: String): SslContext {
        val builder: SslContextBuilder = GrpcSslContexts.forClient()
        if (trustCertCollectionFilePath != null) {
            builder.trustManager(File(trustCertCollectionFilePath))
        }
        if (clientCertChainFilePath != null && clientPrivateKeyFilePath != null) {
            builder.keyManager(File(clientCertChainFilePath), File(clientPrivateKeyFilePath))
        }
        return builder.build()
    }


    constructor(host: String, port: Int) {
        this.channel = NettyChannelBuilder.forAddress(host, port)
//                .overrideAuthority("foo.test.google.fr")  /* Only for using provided test certs. */
                .sslContext(
                        buildSslContext(
                                "/home/ivan/keys/ca.crt",
                                "/home/ivan/keys/client.crt",
                                "/home/ivan/keys/client.pem"))
                .build()
        blockingStub = TransaqConnectorGrpc.newBlockingStub(channel)
    }

    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        private val logger = Logger.getLogger(TransaqGrpcClientExample::class.java.getName())

    }

}