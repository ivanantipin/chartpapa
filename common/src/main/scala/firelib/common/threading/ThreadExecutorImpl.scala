package firelib.common.threading

import java.util.concurrent._

import org.slf4j.LoggerFactory

class ThreadExecutorImpl(val threadsNumber: Int = 1, var threadName: String = "pipeline_") extends ThreadExecutor with ThreadFactory {

    private val executor = new ThreadPoolExecutor(threadsNumber, threadsNumber, 1, TimeUnit.SECONDS, new LinkedBlockingQueue[Runnable](), this)

    private var threadcounter = 0

    val log = LoggerFactory.getLogger(threadName)

    def execute(act: () => Unit) = {
        executor.execute(new Runnable {
            override def run = {
                try {
                    act()
                } catch {
                    case e : Throwable => log.error("exception in pipeline ",e)
                }
            }
        })
    }

    def start() : ThreadExecutor ={
        return this
    }

    def shutdown() = {
        executor.shutdown()
        executor.awaitTermination(100, TimeUnit.DAYS)
    }


    override def newThread(r: Runnable): Thread = {
        val ret: Thread = Executors.defaultThreadFactory().newThread(r)
        threadcounter += 1
        ret.setName(s"${threadName}_$threadcounter")
        return ret
    }
}
