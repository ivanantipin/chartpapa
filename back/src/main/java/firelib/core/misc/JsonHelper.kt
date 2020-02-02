package firelib.core.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object JsonHelper {

    var mapper = ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT).registerModule(KotlinModule())

    fun toJsonString(obj: Any ): String {
        return StringWriter().apply {
            mapper.writeValue(this, obj)
        }.toString()
    }


    fun serialize(value: Any, fileName : Path): Unit {
        val writer = StringWriter()
        mapper.writeValue(writer, value)
        Files.write(fileName, listOf(writer.toString()), StandardOpenOption.CREATE)
    }

    fun <T> deserialize(fileName: Path, clazz : Class<T>): T =
        mapper.readValue(fileName.toFile(), clazz)
}