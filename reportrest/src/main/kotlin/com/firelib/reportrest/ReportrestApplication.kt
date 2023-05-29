package com.firelib.reportrest

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class ReportrestApplication

fun main(args: Array<String>) {
	initDatabase()
	runApplication<ReportrestApplication>(*args)
}
