package firelib.common.misc

import java.io.StringWriter
import java.nio.file.{Files, Path, StandardOpenOption}

import com.fasterxml.jackson.databind.{ObjectMapper, SerializationFeature}
import com.fasterxml.jackson.module.scala.DefaultScalaModule

import scala.collection.JavaConversions._

object jsonHelper {

    val mapper = new ObjectMapper()
    mapper.enable(SerializationFeature.INDENT_OUTPUT)
    mapper.registerModule(DefaultScalaModule)

    def toJsonString(obj: Any ): String = {
        val writer = new StringWriter()
        mapper.writeValue(writer, obj)
        writer.toString
    }


    def serialize(value: Any, fileName : Path): Unit = {
        val writer = new StringWriter()
        mapper.writeValue(writer, value)
        Files.write(fileName, List(writer.toString), StandardOpenOption.CREATE)
    }

    def deserialize[T](fileName: Path, clazz : Class[T]): T =
        mapper.readValue(fileName.toFile, clazz)
}
