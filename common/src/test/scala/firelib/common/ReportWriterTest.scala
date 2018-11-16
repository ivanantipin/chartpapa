package firelib.common

import java.nio.file.Paths

import firelib.common.report.reportWriter
import org.junit.{Assert, Test}


class ReportWriterTest {

    @Test
    def testCopyJarResourceToReal(): Unit ={
        val resourcePath = this.getClass.getClassLoader.getResource(".").getPath
        reportWriter.copyJarFileToReal("/test/test.txt", Paths.get(resourcePath,"test1.txt").toFile.toString)
        Assert.assertTrue(Paths.get(resourcePath,"/test1.txt").toFile.exists())
    }

}
