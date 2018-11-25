package firelib.common.misc

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.SerializationFeature
import java.io.StringWriter
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

object jsonHelper {

    val mapper = ObjectMapper()

    init {
        mapper.enable(SerializationFeature.INDENT_OUTPUT)
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