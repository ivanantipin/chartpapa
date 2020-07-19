package com.firelib.transaq

import com.firelib.TransaqConnectorGrpc
import firelib.core.store.GlobalConstants
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Logger


class TransaqClient {
    var channel: io.grpc.ManagedChannel
    val blockingStub: TransaqConnectorGrpc.TransaqConnectorBlockingStub


    private fun buildSslContext(
        trustCertCollectionFilePath: String,
        clientCertChainFilePath: String,
        clientPrivateKeyFilePath: String
    ): SslContext {
        val builder: SslContextBuilder = GrpcSslContexts.forClient()
        builder.trustManager(File(trustCertCollectionFilePath))
        builder.keyManager(File(clientCertChainFilePath), File(clientPrivateKeyFilePath))
        return builder.build()
    }


    constructor(host: String, port: Int) {

        val keysPath = GlobalConstants.rootFolder.resolve("keys").toAbsolutePath().toFile().toString()

        this.channel = NettyChannelBuilder.forAddress(host, port)
            .maxInboundMessageSize(30_000_000)
//                .overrideAuthority("foo.test.google.fr")  /* Only for using provided test certs. */
            .sslContext(
                buildSslContext(
                    "${keysPath}/ca.crt",
                    "${keysPath}/client.crt",
                    "${keysPath}/client.pem"
                )
            ).overrideAuthority("localhost")
            .build()
        blockingStub = TransaqConnectorGrpc.newBlockingStub(channel)
    }

    fun shutdown() {
        channel.shutdown().awaitTermination(5, TimeUnit.SECONDS)
    }

    companion object {
        private val logger = Logger.getLogger(TransaqClient::class.java.getName())

    }
}