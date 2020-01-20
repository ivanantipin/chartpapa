package com.funstat.store


import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap

class SingletonsContainer : AutoCloseable {

    private val singletones = ConcurrentHashMap<String, Any>()
    private val log = LoggerFactory.getLogger(javaClass)

    private val expirable = ConcurrentHashMap<String, CachedUnit>()

    @Synchronized
    operator fun <T : Any> get(name: String, provider : ()->T
                         ): T {
        if (!singletones.containsKey(name)) {
            try {
                val t = provider()
                log.info("created singleton service " + name + " with type " + t.javaClass)
                val existing = singletones.put(name, t)
                if (existing != null) {
                    throw RuntimeException("service already existed $name cyclic dependency")
                }
            } catch (e: Exception) {
                log.error("exception ", e)
                throw RuntimeException(e)
            }

        }
        return singletones[name] as T
    }

    @Throws(Exception::class)
    override fun close() {
        singletones.forEach { n, s ->
            if (s is AutoCloseable) {
                try {
                    s.close()
                } catch (e: Exception) {
                    log.error("failed to close properly singleton service $n", e)
                }

            }
        }

    }

    internal class CachedUnit(var timestamp: Long, var obj: Any)


    @Synchronized
    fun <T : Any> getWithExpiration(name: String, provider: ()->T, minsToExpire: Long): T {
        val ret = (expirable as java.util.Map<String, CachedUnit>).computeIfAbsent(name) { k -> CachedUnit(System.currentTimeMillis(), provider()) }
        if ((System.currentTimeMillis() - ret.timestamp) / 60000 > minsToExpire) {
            try {
                expirable[name] = CachedUnit(System.currentTimeMillis(), provider())
            } catch (e: RuntimeException) {
                log.error("failed to create service, returning previos version", e)
                return expirable[name]!!.obj as T
            }

        }
        return ret.obj as T
    }
}
