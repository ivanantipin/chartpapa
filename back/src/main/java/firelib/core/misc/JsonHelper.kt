package firelib.core.misc

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object JsonHelper {

    var mapper = ObjectMapper().registerModule(KotlinModule()).apply {
        this.setSerializationInclusion(JsonInclude.Include.NON_NULL)
    }

    fun toJsonString(obj: Any): String {
        return mapper.writeValueAsString(obj)
    }

    fun toStringPretty(obj: Any): String {
        return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj)
    }


    fun toJsonBytes(obj: Any): ByteArray {
        return mapper.writeValueAsBytes(obj)
    }


    inline fun <reified T> fromJson(str: String): T {
        return mapper.readValue(str, T::class.java)
    }

    fun serialize(value: Any, fileName: Path): Unit {
        val writer = StringWriter()
        mapper.writeValue(writer, value)
        Files.write(fileName, listOf(writer.toString()), StandardOpenOption.CREATE)
    }
}
