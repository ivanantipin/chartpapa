package firelib.core.misc

import org.springframework.jdbc.core.JdbcTemplate
import org.sqlite.SQLiteConfig
import org.sqlite.SQLiteDataSource
import javax.sql.DataSource

object SqlUtils{
    fun makeSqlStatementFromHeader(table: String, header: Map<String, String>): String {
        val names = header.toList().map { it.first }
        val decl = names.joinToString(separator = ",")
        val vals = names.joinToString(separator = ",") { ":${it}" }
        return "insert or replace into $table ($decl) values ( ${vals} )"
    }



    fun makeCreateSqlStmtFromHeader(table: String, header: Map<String, String>, pk : List<String> = emptyList()): String {
        val t0 = header.toList().map { "${it.first} ${it.second} not NULL" }.joinToString(separator = ",")
        val ppk = if(pk.isEmpty()) "" else ", primary key (${pk.joinToString (separator = ",")})"
        return  "create table if not exists $table ( ${t0} ${ppk} ) ;"
    }

    fun getDsForFile(file: String): SQLiteDataSource {

        val sqLiteConfig = SQLiteConfig()
        //sqLiteConfig.setPragma(Pragma.DATE_STRING_FORMAT, "yyyy-MM-dd HH:mm:ss")

        val ds = SQLiteDataSource(sqLiteConfig)
        ds.url = "jdbc:sqlite:$file?journal_mode=WAL"
        return ds
    }
    fun checkTableExists(ds: DataSource, tableName: String): Boolean {
        return JdbcTemplate(ds).queryForObject(
            "SELECT count(*) FROM sqlite_master WHERE type='table' AND lower(name)='${tableName.toLowerCase()}'",
            Int::class.java
        ) > 0
    }

    fun listAllTables(ds: DataSource): List<String> {
        return JdbcTemplate(ds).queryForList("SELECT name FROM sqlite_master WHERE type='table' ", String::class.java)
    }


}