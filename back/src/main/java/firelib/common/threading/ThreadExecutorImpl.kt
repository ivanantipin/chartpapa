package firelib.common.threading

import java.util.concurrent.*

import org.slf4j.LoggerFactory

class ThreadExecutorImpl(val threadsNumber: Int = 1, var threadName: String = "pipeline_") : ThreadExecutor , ThreadFactory {

    private val executor = ThreadPoolExecutor(threadsNumber, threadsNumber, 1, TimeUnit.SECONDS, LinkedBlockingQueue<Runnable>(), this)

    private var threadcounter = 0

    val log = LoggerFactory.getLogger(threadName)

    override fun execute(act: () -> Unit) {
        executor.execute(object:Runnable {
            override fun run() {
                try {
                    act()
                } catch(e : Exception) {
                    log.error("exception in pipeline ",e)
                }
            }
        })
    }

    override fun start() : ThreadExecutor {
        return this
    }

    override fun shutdown() {
        executor.shutdown()
        executor.awaitTermination(100, TimeUnit.DAYS)
    }


    override fun newThread(r: Runnable): Thread {
        val ret: Thread = Executors.defaultThreadFactory().newThread(r)
        threadcounter += 1
        ret.setName("${threadName}_$threadcounter")
        return ret
    }
}