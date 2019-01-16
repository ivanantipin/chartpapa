package firelib.common.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import firelib.common.config.InstrumentConfig
import firelib.common.reader.MarketDataReaderSql
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object jsonHelper {

    var mapper = ObjectMapper()

    init {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
        mapper.registerModule(KotlinModule())
    }

    fun toJsonString(obj: Any ): String {
        val writer = StringWriter()
        mapper.writeValue(writer, obj)
        return writer.toString()
    }


    fun serialize(value: Any, fileName : Path): Unit {
        val writer = StringWriter()
        mapper.writeValue(writer, value)
        Files.write(fileName, listOf(writer.toString()), StandardOpenOption.CREATE)
    }

    fun <T> deserialize(fileName: Path, clazz : Class<T>): T =
        mapper.readValue(fileName.toFile(), clazz)
}

fun main(args: Array<String>) {
    print(jsonHelper.toJsonString(InstrumentConfig("some", {ff->MarketDataReaderSql(emptyList())})))
}