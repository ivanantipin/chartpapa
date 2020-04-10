package com.firelib.transaq

import com.firelib.Empty
import com.firelib.Str
import com.firelib.TransaqConnectorGrpc
import io.grpc.BindableService
import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver
import java.io.File

fun grpcServerRun(port: Int, services: List<BindableService>) {
    val serverBuilder = ServerBuilder.forPort(port)
    services.forEach { serverBuilder.addService(it) }

    //
    val server = serverBuilder.useTransportSecurity(File("/home/ivan/projects/chartpapa/transaqgate/server.crt"),
        File("/home/ivan/projects/chartpapa/transaqgate/server.pem")
    ).build().start()
    println("Server started, listening on ${port}")
    Runtime.getRuntime().addShutdownHook(object : Thread() {
        override fun run() {
            // Use stderr here since the logger may have been reset by its JVM shutdown hook.
            System.err.println("*** shutting down gRPC server since JVM is shutting down")
            server.shutdown()
            System.err.println("*** server shut down")
        }
    })
    server.awaitTermination()

}

class AAA : TransaqConnectorGrpc.TransaqConnectorImplBase() {

    override fun connect(request: Empty?, responseObserver: StreamObserver<Str>?) {
        super.connect(request, responseObserver)
    }

    override fun sendCommand(request: Str?, responseObserver: StreamObserver<Str>?) {
        super.sendCommand(request, responseObserver)
    }

}

fun main() {
    grpcServerRun(5001, listOf(AAA()))
}