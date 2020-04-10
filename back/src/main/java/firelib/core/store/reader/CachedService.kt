package firelib.core.store.reader

import firelib.core.domain.Timed
import firelib.core.misc.toStandardString
import firelib.core.store.reader.binary.BinaryReader
import firelib.core.store.reader.binary.BinaryReaderRecordDescriptor
import firelib.core.store.reader.binary.BinaryWriter
import org.apache.commons.io.FileUtils
import java.nio.file.Path
import java.nio.file.Paths
import java.time.Instant


class CachedService(val cacheDirectory : String) {

    private val rootDir: Path = Paths.get(cacheDirectory)

    private fun makeKeyFolder(fn : String): String {
        val path: Path = Paths.get(fn).toAbsolutePath()
        return "${path.getParent().getFileName()}_${path.getFileName()}"
    }

    private fun makeTimeKey(startTime : Instant, endTime : Instant ): String {
        val st = startTime.toStandardString()
        val et = endTime.toStandardString()
        return "$st-$et"
    }


    fun <T : Timed>  checkPresent(fn : String, startTime : Instant, endTime : Instant, desc: BinaryReaderRecordDescriptor<T>): MarketDataReader<T>? {

        val keyFolder: String = makeKeyFolder(fn)
        val timeKey = makeTimeKey(startTime,endTime)


        val resolve: Path = rootDir.resolve(keyFolder).resolve(timeKey)

        if(!resolve.toFile().exists()){
            return null
        }else{
            return BinaryReader<T>(resolve.toString(),desc)
        }
    }

    fun <T : Timed> write(fn : String, reader : MarketDataReader<T>, tt : BinaryReaderRecordDescriptor<T>) : MarketDataReader<T> {

        val keyFolder: String = makeKeyFolder(fn)
        val timeKey = makeTimeKey(reader.startTime(),reader.endTime())

        FileUtils.deleteDirectory(rootDir.resolve(keyFolder).toFile())
        FileUtils.forceMkdir(rootDir.resolve(keyFolder).toFile())

        val resolve: Path = rootDir.resolve(keyFolder).resolve(timeKey)

        val cachedFile: String = resolve.toAbsolutePath().toString()
        System.out.println("caching $cachedFile")
        val writer: BinaryWriter<T> = BinaryWriter<T>(cachedFile,tt)
        while(reader.read()){
            writer.write(reader.current())
        }
        writer.flush()
        return this.checkPresent(fn,reader.startTime(),reader.endTime(),tt)!!
    }

}