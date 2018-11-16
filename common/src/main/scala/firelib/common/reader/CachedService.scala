package firelib.common.reader

import java.nio.file.{Path, Paths}
import java.time.Instant

import firelib.common.misc.DateUtils
import firelib.common.reader.binary.{BinaryReader, BinaryReaderRecordDescriptor, BinaryWriter}
import firelib.domain.Timed
import org.apache.commons.io.FileUtils


class CachedService(val cacheDirectory : String) extends DateUtils{

    private val rootDir: Path = Paths.get(cacheDirectory)

    private def makeKeyFolder(fn : String): String ={
        val path: Path = Paths.get(fn).toAbsolutePath
        s"${path.getParent.getFileName}_${path.getFileName}"
    }

    private def makeTimeKey(startTime : Instant, endTime : Instant ): String ={
        val st = startTime.toStandardString
        val et = endTime.toStandardString
        s"$st-$et"
    }


    def checkPresent[T <: Timed](fn : String, startTime : Instant, endTime : Instant, desc: BinaryReaderRecordDescriptor[T]): Option[MarketDataReader[T]] ={

        val keyFolder: String = makeKeyFolder(fn)
        val timeKey = makeTimeKey(startTime,endTime)


        val resolve: Path = rootDir.resolve(keyFolder).resolve(timeKey)

        if(!resolve.toFile.exists()){
            None
        }else{
            Option(new BinaryReader[T](resolve.toString,desc))
        }
    }

    def write[T <: Timed](fn : String, reader : MarketDataReader[T], tt : BinaryReaderRecordDescriptor[T]) : MarketDataReader[T] = {

        val keyFolder: String = makeKeyFolder(fn)
        val timeKey = makeTimeKey(reader.startTime(),reader.endTime())

        FileUtils.deleteDirectory(rootDir.resolve(keyFolder).toFile)
        FileUtils.forceMkdir(rootDir.resolve(keyFolder).toFile)

        val resolve: Path = rootDir.resolve(keyFolder).resolve(timeKey)

        val cachedFile: String = resolve.toAbsolutePath.toString
        System.out.println(s"caching $cachedFile")
        val writer: BinaryWriter[T] = new BinaryWriter[T](cachedFile,tt)
        while(reader.read()){
            writer.write(reader.current)
        }
        writer.flush()
        checkPresent(fn,reader.startTime(),reader.endTime(),tt).get
    }

}
