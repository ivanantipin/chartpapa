package com.firelib.transaq

import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContext
import io.grpc.netty.shaded.io.netty.handler.ssl.SslContextBuilder
import java.io.File


fun main() {

    fun buildSslContext(trustCertCollectionFilePath: String,
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


    val channel = NettyChannelBuilder.forAddress("192.168.0.10", 50052)

//    val server = serverBuilder.useTransportSecurity(),
//        File("/home/ivan/projects/chartpapa/transaqgate/client.pem")
//
//        .maxInboundMessageSize(30_000_000)
//                .overrideAuthority("foo.test.google.fr")  /* Only for using provided test certs. */

        .sslContext(
            buildSslContext(
                "/home/ivan/transaq/keys/ca.crt",
                "/home/ivan/transaq/keys/client.crt",
                "/home/ivan/transaq/keys/client.pem")).overrideAuthority("localhost")
        .build()
    TransaqConnectorGrpc.newBlockingStub(channel).sendCommand(Str.newBuilder().setTxt("aaa").build())

}