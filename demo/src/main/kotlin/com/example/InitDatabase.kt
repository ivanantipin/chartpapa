package com.example

import firelib.common.Trades
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import java.nio.file.Paths

fun initDatabase() {
    val path = Paths.get("/home/ivan/projects/chartpapa/market_research/report_out/report.db")

    Database.connect(
        "jdbc:sqlite:${path.toAbsolutePath()}?journal_mode=WAL",
        driver = "org.sqlite.JDBC"
    )

    transaction {
        SchemaUtils.createMissingTablesAndColumns(Trades)
    }
}