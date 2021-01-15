package com.example

import io.micronaut.runtime.Micronaut.*
import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.info.*

@OpenAPIDefinition(
    info = Info(
        title = "demo",
        version = "0.0"
    )
)

object Application {

    @JvmStatic
    fun main(args: Array<String>) {
        initDatabase()
        build()
            .args(*args)
            .packages("firelib.stockviz.api")
            .start()
    }
}